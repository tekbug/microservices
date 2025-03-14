package com.athena.v2.teachers.listeners;

import com.athena.v2.teachers.models.Events;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TeacherEventListener {

    @RabbitListener(queues = "teacher.created")
    public void handleTeacherCreation(Events events) {
        // TODO: Implement email service for confirming creation of a teacher
    }

    @RabbitListener(queues = "teacher.updated")
    public void handleTeacherUpdate(Events events) {
        // TODO: Implement email service for confirming the updated information of a teacher
    }

    @RabbitListener(queues = "teacher.deleted")
    public void handleTeacherDeletion(Events events) {
        // TODO: Implement email service for confirming the deletion of a teacher
    }
}
