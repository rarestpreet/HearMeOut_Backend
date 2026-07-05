package com.project.hearmeout_backend.common_lib.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageData {
  private boolean hasMore;
  private long total;
  private int offset;
  private int limit;
}
