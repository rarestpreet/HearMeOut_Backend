package com.project.hearmeout_backend.service.implementation;

import com.project.hearmeout_backend.dto.request.post_request.AcceptAnswerRequestDTO;
import com.project.hearmeout_backend.dto.request.post_request.AnswerSubmitRequestDTO;
import com.project.hearmeout_backend.dto.request.post_request.QuestionSubmitRequestDTO;
import com.project.hearmeout_backend.dto.response.comment_response.CommentResponseDTO;
import com.project.hearmeout_backend.dto.response.post_response.PostAnswerResponseDTO;
import com.project.hearmeout_backend.dto.response.post_response.QuestionPostResponseDTO;
import com.project.hearmeout_backend.dto.response.tag_response.TagResponseDTO;
import com.project.hearmeout_backend.exception.InvalidOperationException;
import com.project.hearmeout_backend.exception.PostNotFoundException;
import com.project.hearmeout_backend.exception.TagNotFoundException;
import com.project.hearmeout_backend.exception.UserNotFoundException;
import com.project.hearmeout_backend.mapper.PostMapper;
import com.project.hearmeout_backend.model.Post;
import com.project.hearmeout_backend.model.Tag;
import com.project.hearmeout_backend.model.User;
import com.project.hearmeout_backend.model.Vote;
import com.project.hearmeout_backend.model.enums.PostStatus;
import com.project.hearmeout_backend.model.enums.PostType;
import com.project.hearmeout_backend.model.enums.VoteType;
import com.project.hearmeout_backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostServiceImpl {

    private final PostRepository postRepo;
    private final TagRepository tagRepo;
    private final UserServiceImpl userServiceImpl;
    private final VoteRepository voteRepo;
    private final CommentRepository commentRepo;
    private final UserRepository userRepo;
    private final CommentServiceImpl commentServiceImpl;

    public Post checkAndGetPost(Long postId)
            throws PostNotFoundException {
        return postRepo.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post not found with id: " + postId));
    }

    @Transactional
    public void postNewQuestion(QuestionSubmitRequestDTO questionSubmitRequestDTO, Long userId)
            throws UserNotFoundException, TagNotFoundException {
        User author = userServiceImpl.checkAndGetUserByUserId(userId);
        List<Tag> tags = tagRepo.findAllById(questionSubmitRequestDTO.getTagIds());

        if (questionSubmitRequestDTO.getTagIds().size() != tags.size()) {
            throw new TagNotFoundException("Some tags do not exist");
        }

        author.setReputation(author.getReputation() + 4);
        userRepo.save(author);

        Post newPost = PostMapper.questionToPostEntity(questionSubmitRequestDTO, author, tags);
        postRepo.save(newPost);
    }

    @Transactional
    public void postNewAnswer(Long postId, AnswerSubmitRequestDTO answerSubmitRequestDTO, Long userId)
            throws UserNotFoundException, PostNotFoundException {
        Post parent = checkAndGetPost(postId);

        if (Objects.equals(userId, parent.getAuthor().getId())) {
            throw new InvalidOperationException("You cannot answer your own questions.");
        }

        if (Objects.equals(parent.getPostType(),PostType.ANSWER)) {
            throw new InvalidOperationException("You can only answer questions.");
        }

        if(Objects.equals(parent.getStatus(), PostStatus.CLOSED)){
            throw new InvalidOperationException("Question is already closed.");
        }

        User author = userServiceImpl.checkAndGetUserByUserId(userId);
        author.setReputation(author.getReputation() + 6);
        userRepo.save(author);

        parent.setStatus(PostStatus.ANSWERED);
        Post newPost = PostMapper.answerToPostEntity(answerSubmitRequestDTO, parent, author);
        postRepo.save(newPost);
    }

    @Transactional(readOnly = true)
    public QuestionPostResponseDTO getQuestionPost(Long postId, Long currUserId, String currUsername)
            throws PostNotFoundException {
        QuestionPostResponseDTO currPostDTO = postRepo.findQuestionPostDetailsDTO(postId, PostType.QUESTION)
                .orElseThrow(() -> new PostNotFoundException("Post not found with id: " + postId));

        Vote currUserVoteOnQuestion = voteRepo.findByPostIdAndUserId(postId, currUserId).orElse(null);

        List<PostAnswerResponseDTO> answers = postRepo.findAnswersDTOByQuestionId(postId, PostType.ANSWER);

        List<Long> answerIds = answers.stream().map(PostAnswerResponseDTO::getPostId).toList();
        List<CommentResponseDTO> allAnswerComments = answerIds.isEmpty() ? List.of() :
                answerIds.stream()
                        .map(commentRepo::findCommentsDTOByPostId)
                        .flatMap(List::stream)
                        .toList();

        answers.forEach(answer -> {
            Vote currUserVoteOnAnswer = voteRepo.findByPostIdAndUserId(answer.getPostId(), currUserId).orElse(null);
            answer.setVoted(currUserVoteOnAnswer != null);
            answer.setOperable(answer.getAuthorUsername().equals(currUsername));
            answer.setVoteType(currUserVoteOnAnswer != null ? currUserVoteOnAnswer.getVoteType() : null);

            List<CommentResponseDTO> answerComments = allAnswerComments.stream()
                    .filter(c -> c.getNavigationPostId().equals(answer.getPostId()))
                    .peek(c -> {
                        c.setOperable(c.getAuthorUsername().equals(currUsername));
                    })
                    .toList();
            answer.setComments(answerComments);
        });
        List<TagResponseDTO> tags = tagRepo.findTagsDTOByPostId(postId);

        List<CommentResponseDTO> comments = commentRepo.findCommentsDTOByPostId(postId);
        comments.forEach(c -> c.setOperable(c.getAuthorUsername().equals(currUsername)));

        currPostDTO.setVoted(currUserVoteOnQuestion != null);
        currPostDTO.setVoteType(currUserVoteOnQuestion != null ? currUserVoteOnQuestion.getVoteType() : null);
        currPostDTO.setAnswers(answers);
        currPostDTO.setTags(tags);
        currPostDTO.setComments(comments);
        currPostDTO.setOperable(currPostDTO.getAuthorUsername().equals(currUsername));

        return currPostDTO;
    }

    @Transactional
    public void handleAnswerStatus(AcceptAnswerRequestDTO acceptAnswerRequestDTO, Long currUserId) {
        Post question = checkAndGetPost(acceptAnswerRequestDTO.getQuestionId());
        Post answer = checkAndGetPost(acceptAnswerRequestDTO.getAnswerId());
        User answerAuthor = userServiceImpl.checkAndGetUserByUserId(answer.getAuthor().getId());
        User questionAuthor = userServiceImpl.checkAndGetUserByUserId(question.getAuthor().getId());

        if (!Objects.equals(PostType.ANSWER, answer.getPostType()) ||
                !Objects.equals(PostType.QUESTION, question.getPostType())) {
            throw new InvalidOperationException("Operation cannot be performed for the requested post.");
        }

        if (!Objects.equals(questionAuthor.getId(), currUserId)) {
            throw new InvalidOperationException("You can only perform operations for self-posted questions.");
        }

        if (!Objects.equals(answer.getParent().getId(), question.getId())) {
            throw new InvalidOperationException("This answer does not belong to the specified question.");
        }

        PostStatus currQuestionStatus = question.getStatus();
        PostStatus currAnswerStatus = answer.getStatus();

        // question is not resolved, author intend to close the question and accept an answer
        if (Objects.equals(currQuestionStatus, PostStatus.ANSWERED)) {
            questionAuthor.setReputation(questionAuthor.getReputation() + 3);
            answerAuthor.setReputation(answerAuthor.getReputation() + 7);
            answer.setScore(answer.getScore() + 5);
            question.setStatus(PostStatus.CLOSED);
            answer.setStatus(PostStatus.ACCEPTED);

            postRepo.save(question);
            postRepo.save(answer);
            userRepo.save(answerAuthor);
            userRepo.save(questionAuthor);

            return;
        }

        // question is already resolved, but author intend to reopen the question for new answers
        if (Objects.equals(currAnswerStatus, PostStatus.ACCEPTED)) {
            questionAuthor.setReputation(questionAuthor.getReputation() - 3);
            answerAuthor.setReputation(answerAuthor.getReputation() - 7);
            answer.setScore(answer.getScore() - 5);
            question.setStatus(PostStatus.ANSWERED);
            answer.setStatus(PostStatus.UNREVIEWED);
        }
        // question is already resolved, but author intend to accept another answer and keep the question closed
        else {
            List<Post> acceptedAnswers = question.getAnswers().stream()
                    .filter(ans -> Objects.equals(ans.getStatus(), PostStatus.ACCEPTED))
                    .toList();

            if(acceptedAnswers.size() != 1) {
               log.error("Invalid accepted answers for a closed question {}", question.getId());
            }
            Post olderAcceptedAnswer = acceptedAnswers.getFirst();
            User olderAcceptedAnswerAuthor = userServiceImpl.checkAndGetUserByUserId(olderAcceptedAnswer.getAuthor().getId());

            olderAcceptedAnswer.setStatus(PostStatus.UNREVIEWED);
            olderAcceptedAnswer.setScore(olderAcceptedAnswer.getScore() - 5);
            olderAcceptedAnswerAuthor.setReputation(olderAcceptedAnswerAuthor.getReputation() - 7);

            postRepo.save(olderAcceptedAnswer);
            userRepo.save(olderAcceptedAnswerAuthor);

            answerAuthor.setReputation(answerAuthor.getReputation() + 7);
            answer.setScore(answer.getScore() + 5);
            answer.setStatus(PostStatus.ACCEPTED);
        }

        postRepo.save(question);
        postRepo.save(answer);
        userRepo.save(answerAuthor);
        userRepo.save(questionAuthor);
    }

    @Transactional
    public void updateQuestion(Long postId, QuestionSubmitRequestDTO questionSubmitRequestDTO, Long currUserId) {
        Post question = checkAndGetPost(postId);

        if (!Objects.equals(question.getPostType(), PostType.QUESTION)) {
            throw new InvalidOperationException("Invalid operation on post: " + question.getPostType());
        }

        if (!Objects.equals(question.getAuthor().getId(), currUserId)) {
            throw new InvalidOperationException("You cannot modify this question.");
        }

        if (Objects.equals(question.getStatus(), PostStatus.CLOSED)) {
            throw new InvalidOperationException("Cannot modify this question, already resolved.");
        }

        List<Tag> tags = tagRepo.findAllById(questionSubmitRequestDTO.getTagIds());

        if (tags.size() != questionSubmitRequestDTO.getTagIds().size()) {
            throw new TagNotFoundException("Some tags do not exist");
        }

        question.setTitle(questionSubmitRequestDTO.getTitle());
        question.setBody(questionSubmitRequestDTO.getBody());
        question.setTags(tags);
        question.markUpdatedAt();

        postRepo.save(question);
    }

    @Transactional
    public void updateAnswer(Long postId, AnswerSubmitRequestDTO answerSubmitRequestDTO, Long currUserId) {
        Post answer = checkAndGetPost(postId);

        if (!Objects.equals(answer.getPostType(), PostType.ANSWER)) {
            throw new InvalidOperationException("Invalid operation on post: " + answer.getPostType());
        }

        if (!Objects.equals(answer.getAuthor().getId(), currUserId)) {
            throw new InvalidOperationException("You cannot modify this answer.");
        }

        if (Objects.equals(answer.getStatus(), PostStatus.ACCEPTED)) {
            throw new InvalidOperationException("Cannot modify this answer, is it finalized.");
        }

        answer.setBody(answerSubmitRequestDTO.getBody());
        answer.markUpdatedAt();

        postRepo.save(answer);
    }

    @Transactional
    public void deleteAnswer(Long postId, Long currUserId) {
        Post answer = checkAndGetPost(postId);

        if (!Objects.equals(answer.getPostType(), PostType.ANSWER)) {
            throw new InvalidOperationException("Invalid operation on post: " + answer.getPostType());
        }

        if (!Objects.equals(answer.getAuthor().getId(), currUserId)) {
            throw new InvalidOperationException("You cannot delete this answer.");
        }

        if (Objects.equals(answer.getStatus(), PostStatus.ACCEPTED)) {
            throw new InvalidOperationException("Cannot delete this answer, it is finalized.");
        }

        User answerAuthor = userServiceImpl.checkAndGetUserByUserId(answer.getAuthor().getId());
        List<Vote> votes = voteRepo.findAllByPostId(postId);

        votes.forEach(vote -> {
            vote.getUser().setReputation(vote.getUser().getReputation() - 1);

            if (Objects.equals(vote.getVoteType(), VoteType.UPVOTE)) {
                answerAuthor.setReputation(answerAuthor.getReputation() - 1);
            } else {
                answerAuthor.setReputation(answerAuthor.getReputation() + 1);
            }

            voteRepo.save(vote);
        });

        answer.getComments().forEach(comment -> {
            commentServiceImpl.removeComment(comment.getId(), comment.getAuthor().getId());
        });

        answerAuthor.setReputation(answerAuthor.getReputation() - 6);

        postRepo.delete(answer);
    }

    @Transactional
    public void deleteQuestion(Long postId, Long currUserId) {
        Post question = checkAndGetPost(postId);

        if (!Objects.equals(question.getPostType(), PostType.QUESTION)) {
            throw new InvalidOperationException("Invalid operation on post: " + question.getPostType());
        }

        if (!Objects.equals(question.getAuthor().getId(), currUserId)) {
            throw new InvalidOperationException("You cannot delete this question.");
        }

        if (Objects.equals(question.getStatus(), PostStatus.CLOSED)) {
            throw new InvalidOperationException("Cannot delete this question, already resolved.");
        }

        User questionAuthor = userServiceImpl.checkAndGetUserByUserId(question.getAuthor().getId());
        List<Vote> votes = voteRepo.findAllByPostId(postId);

        question.getAnswers().forEach(answer -> {
            deleteAnswer(answer.getId(), answer.getAuthor().getId());
        });

        question.getComments().forEach(comment -> {
            commentServiceImpl.removeComment(comment.getId(), comment.getAuthor().getId());
        });

        votes.forEach(vote -> {
            vote.getUser().setReputation(vote.getUser().getReputation() - 1);

            if (Objects.equals(vote.getVoteType(), VoteType.UPVOTE)) {
                questionAuthor.setReputation(questionAuthor.getReputation() - 1);
            } else {
                questionAuthor.setReputation(questionAuthor.getReputation() + 1);
            }

            voteRepo.save(vote);
        });

        questionAuthor.setReputation(questionAuthor.getReputation() - 4);

        postRepo.delete(question);
    }
}
