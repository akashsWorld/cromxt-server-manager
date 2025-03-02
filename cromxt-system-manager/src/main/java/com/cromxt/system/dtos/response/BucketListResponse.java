package com.cromxt.system.dtos.response;

import com.cromxt.common.crombucket.dtos.BaseResponse;
import com.cromxt.common.crombucket.dtos.CromxtResponseStatus;
import com.cromxt.system.dtos.BucketDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class BucketListResponse extends BaseResponse {
    private List<BucketDTO> buckets;

    public BucketListResponse(CromxtResponseStatus status, List<BucketDTO> buckets) {
        super(status);
        this.buckets = buckets;
    }
}
