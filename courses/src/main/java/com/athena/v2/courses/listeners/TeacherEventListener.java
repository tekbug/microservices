package com.athena.v2.courses.listeners;

import com.athena.v2.courses.models.Events;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TeacherEventListener {

    @RabbitListener(queues = "course.created")
    public void handleTeacherCreation(Events events) {
        // TODO: Implement email service for confirming creation of a teacher
    }

    @RabbitListener(queues = "course.updated")
    public void handleTeacherUpdate(Events events) {
        // TODO: Implement email service for confirming the updated information of a teacher
    }

    @RabbitListener(queues = "course.deleted")
    public void handleTeacherDeletion(Events events) {
        // TODO: Implement email service for confirming the deletion of a teacher
    }
}
