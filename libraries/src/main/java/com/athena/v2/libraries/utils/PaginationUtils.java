package com.athena.v2.libraries.utils;

import com.athena.v2.libraries.exceptions.PageCannotBeNegativeException;
import lombok.experimental.UtilityClass;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@UtilityClass
public class PaginationUtils {

    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 10;
    public static final String DEFAULT_SORT_DIRECTION = "ASC";

    public static Pageable createPageable(int page, int size, String sortBy, String direction) {
        if (page < 0) {
            throw new PageCannotBeNegativeException("page index cannot be a zero!");
        }
        if (size < 1) {
            throw new PageCannotBeNegativeException("page size cannot be a zero!");
        }
        if (size > MAX_PAGE_SIZE) {
            throw new PageCannotBeNegativeException("page size cannot be more than " + MAX_PAGE_SIZE);
        }

        Sort.Direction sortDirection = direction != null ? Sort.Direction.fromString(direction.toUpperCase()) : Sort.Direction.fromString(DEFAULT_SORT_DIRECTION);
        Sort sort = Sort.by(sortDirection, sortBy != null ? sortBy : DEFAULT_SORT_DIRECTION);

        return PageRequest.of(page, size, sort);
    }
}
