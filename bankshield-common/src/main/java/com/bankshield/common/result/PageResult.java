package com.bankshield.common.result;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 分页响应结果类
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "分页响应结果")
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "数据列表")
    private List<T> list;

    @ApiModelProperty(value = "总记录数")
    private long total;

    @ApiModelProperty(value = "当前页码")
    private int pageNum;

    @ApiModelProperty(value = "每页数量")
    private int pageSize;

    @ApiModelProperty(value = "总页数")
    private int pages;

    @ApiModelProperty(value = "是否有下一页")
    private boolean hasNextPage;

    @ApiModelProperty(value = "是否有上一页")
    private boolean hasPreviousPage;

    public PageResult(List<T> list, long total, int pageNum, int pageSize) {
        this.list = list;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.pages = (int) Math.ceil((double) total / pageSize);
        this.hasNextPage = pageNum < pages;
        this.hasPreviousPage = pageNum > 1;
    }

    /**
     * 成功响应
     */
    public static <T> PageResult<T> success(List<T> list, long total, int pageNum, int pageSize) {
        return new PageResult<>(list, total, pageNum, pageSize);
    }

    /**
     * 失败响应
     */
    public static <T> PageResult<T> error(String message) {
        throw new RuntimeException(message);
    }
}