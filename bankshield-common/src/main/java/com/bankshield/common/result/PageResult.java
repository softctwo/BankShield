package com.bankshield.common.result;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果类
 */
public class PageResult<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 总记录数
     */
    private Long total;
    
    /**
     * 数据列表
     */
    private List<T> list;
    
    /**
     * 当前页码
     */
    private Integer pageNum;
    
    /**
     * 每页大小
     */
    private Integer pageSize;
    
    /**
     * 总页数
     */
    private Integer pages;
    
    public PageResult() {
    }
    
    public PageResult(Long total, List<T> list) {
        this.total = total;
        this.list = list;
    }
    
    public PageResult(Long total, List<T> list, Integer pageNum, Integer pageSize) {
        this.total = total;
        this.list = list;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        if (pageSize != null && pageSize > 0) {
            this.pages = (int) ((total + pageSize - 1) / pageSize);
        }
    }
    
    /**
     * 创建分页结果
     */
    public static <T> PageResult<T> of(Long total, List<T> list) {
        return new PageResult<>(total, list);
    }
    
    /**
     * 创建分页结果（带页码信息）
     */
    public static <T> PageResult<T> of(Long total, List<T> list, Integer pageNum, Integer pageSize) {
        return new PageResult<>(total, list, pageNum, pageSize);
    }
    
    /**
     * 创建成功的分页结果
     */
    public static <T> PageResult<T> success(List<T> list, Long total, Integer pageNum, Integer pageSize) {
        return new PageResult<>(total, list, pageNum, pageSize);
    }
    
    /**
     * 创建成功的分页结果（不带页码信息）
     */
    public static <T> PageResult<T> success(List<T> list, Long total) {
        return new PageResult<>(total, list);
    }
    
    /**
     * 创建失败的分页结果
     */
    public static <T> PageResult<T> error(String message) {
        PageResult<T> result = new PageResult<>();
        result.setTotal(0L);
        result.setList(null);
        return result;
    }
    
    // Getter and Setter
    public Long getTotal() {
        return total;
    }
    
    public void setTotal(Long total) {
        this.total = total;
    }
    
    public List<T> getList() {
        return list;
    }
    
    public void setList(List<T> list) {
        this.list = list;
    }
    
    public Integer getPageNum() {
        return pageNum;
    }
    
    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }
    
    public Integer getPageSize() {
        return pageSize;
    }
    
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
    
    public Integer getPages() {
        return pages;
    }
    
    public void setPages(Integer pages) {
        this.pages = pages;
    }
}
