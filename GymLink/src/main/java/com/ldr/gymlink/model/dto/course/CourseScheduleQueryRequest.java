package com.ldr.gymlink.model.dto.course;

import com.ldr.gymlink.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class CourseScheduleQueryRequest extends PageRequest implements Serializable {
    private Long courseId;
    private Integer status;

    private static final long serialVersionUID = 1L;
}
