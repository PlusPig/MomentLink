package com.keyy.momentlinkback.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    private List<T> list;      // 数据列表
    private Long total;        // 总记录数
    private Integer page;      // 当前页码
    private Integer size;      // 每页大小
    private Integer totalPages; // 总页数
    private Boolean hasNext;   // 是否有下一页
    private Boolean hasPrev;   // 是否有上一页

    public static <T> PageResult<T> of(List<T> list, Long total, Integer page, Integer size) {
        PageResult<T> result = new PageResult<>();
        result.setList(list);
        result.setTotal(total);
        result.setPage(page);
        result.setSize(size);

        // 计算总页数
        int totalPages = (int) Math.ceil((double) total / size);
        result.setTotalPages(totalPages);

        // 判断是否有上一页和下一页
        result.setHasNext(page < totalPages);
        result.setHasPrev(page > 1);

        return result;
    }

    public static <T> PageResult<T> empty(Integer page, Integer size) {
        return PageResult.<T>builder()
                .list(List.of())
                .total(0L)
                .page(page)
                .size(size)
                .totalPages(0)
                .hasNext(false)
                .hasPrev(false)
                .build();
    }
}
