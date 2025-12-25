package com.bankshield.ai.deep;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;

/**
 * Attention注意力机制
 * 
 * 功能：
 * 1. 自注意力（Self-Attention）：计算序列内部的关联权重
 * 2. 多头注意力（Multi-Head Attention）：从多个角度学习关联
 * 3. 位置编码（Positional Encoding）：保留时序信息
 * 
 * 应用场景：
 * - 重点关注异常时间点
 * - 识别关键行为模式
 * - 提升预测可解释性
 */
@Slf4j
public class AttentionMechanism {
    
    private static final double DROPOUT_RATE = 0.1;
    
    /**
     * 自注意力计算
     * 
     * @param query 查询向量 [batch, seq_len, dim]
     * @param key 键向量 [batch, seq_len, dim]
     * @param value 值向量 [batch, seq_len, dim]
     * @return 注意力输出和权重
     */
    public AttentionOutput selfAttention(INDArray query, INDArray key, INDArray value) {
        int dim = (int)query.size(2);
        
        // 计算注意力分数: Q * K^T / sqrt(dim)
        INDArray scores = query.mmul(key.permute(0, 2, 1));
        scores = scores.div(Math.sqrt(dim));
        
        // Softmax归一化
        INDArray attentionWeights = softmax(scores);
        
        // Attention Dropout
        attentionWeights = dropout(attentionWeights, DROPOUT_RATE);
        
        // 加权求和: Attention * V
        INDArray output = attentionWeights.mmul(value);
        
        return new AttentionOutput(output, attentionWeights);
    }
    
    /**
     * 多头注意力
     */
    public MultiHeadOutput multiHeadAttention(INDArray query, INDArray key, INDArray value, int numHeads) {
        int batchSize = (int)query.size(0);
        int seqLen = (int)query.size(1);
        int dim = (int)query.size(2);
        int headDim = dim / numHeads;
        
        // 分割成多个头
        INDArray queryMultiHead = splitHeads(query, batchSize, numHeads, headDim);
        INDArray keyMultiHead = splitHeads(key, batchSize, numHeads, headDim);
        INDArray valueMultiHead = splitHeads(value, batchSize, numHeads, headDim);
        
        // 对每个头应用自注意力
        AttentionOutput attention = selfAttention(queryMultiHead, keyMultiHead, valueMultiHead);
        
        // 合并头
        INDArray output = mergeHeads(attention.getOutput(), batchSize, seqLen, dim);
        
        return new MultiHeadOutput(output, attention.getWeights());
    }
    
    /**
     * 位置编码（保留时序信息）
     */
    public INDArray positionalEncoding(int seqLen, int dim) {
        INDArray pe = Nd4j.zeros(seqLen, dim);
        
        for (int pos = 0; pos < seqLen; pos++) {
            for (int i = 0; i < dim; i += 2) {
                double angle = pos / Math.pow(10000, (2.0 * i) / dim);
                pe.putScalar(pos, i, Math.sin(angle));
                if (i + 1 < dim) {
                    pe.putScalar(pos, i + 1, Math.cos(angle));
                }
            }
        }
        
        return pe;
    }
    
    /**
     * 分割成多个头
     */
    private INDArray splitHeads(INDArray x, int batchSize, int numHeads, int headDim) {
        return x.reshape(batchSize, -1, numHeads, headDim).permute(0, 2, 1, 3);
    }
    
    /**
     * 合并头
     */
    private INDArray mergeHeads(INDArray x, int batchSize, int seqLen, int dim) {
        return x.permute(0, 2, 1, 3).reshape(batchSize, seqLen, dim);
    }
    
    /**
     * Softmax：e^x / sum(e^x)
     */
    private INDArray softmax(INDArray x) {
        INDArray exp = Nd4j.getExecutioner().execAndReturn(
            new org.nd4j.linalg.api.ops.impl.transforms.strict.Exp(x.dup())
        ).z();
        INDArray sum = exp.sum(1).reshape(exp.size(0), 1);
        return exp.div(sum);
    }
    
    /**
     * Dropout
     */
    private INDArray dropout(INDArray x, double rate) {
        if (rate == 0.0) return x;
        INDArray mask = Nd4j.rand(x.shape()).gt(rate);
        return x.mul(mask).div(1.0 - rate);
    }
    
    @Data
    public static class AttentionOutput {
        private INDArray output;
        private INDArray weights;
        
        public AttentionOutput(INDArray output, INDArray weights) {
            this.output = output;
            this.weights = weights;
        }
    }
    
    @Data
    public static class MultiHeadOutput {
        private INDArray output;
        private INDArray attentionWeights;
        
        public MultiHeadOutput(INDArray output, INDArray attentionWeights) {
            this.output = output;
            this.attentionWeights = attentionWeights;
        }
    }
}