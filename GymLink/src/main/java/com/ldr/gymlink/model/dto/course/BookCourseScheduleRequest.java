package com.ldr.gymlink.model.dto.course;

import lombok.Data;

import java.io.Serializable;

@Data
public class BookCourseScheduleRequest implements Serializable {
    private Long studentId;
    private Long scheduleId;

    private static final long serialVersionUID = 1L;
}
