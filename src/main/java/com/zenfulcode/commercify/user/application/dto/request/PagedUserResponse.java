package com.zenfulcode.commercify.user.application.dto.request;

import com.zenfulcode.commercify.api.product.dto.response.PageInfo;
import com.zenfulcode.commercify.user.application.dto.response.UserProfileResponse;

import java.util.List;

public record PagedUserResponse(
        List<UserProfileResponse> items,
        PageInfo pageInfo
) {
}
