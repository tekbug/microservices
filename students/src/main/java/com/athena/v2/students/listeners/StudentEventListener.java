package com.athena.v2.students.listeners;

import com.athena.v2.students.models.Events;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class StudentEventListener {

    @RabbitListener(queues = "student.created")
    public void handleStudentCreation(Events events) {
        // TODO: Implement email service for confirming creation of a student
    }

    @RabbitListener(queues = "student.updated")
    public void handleStudentUpdate(Events events) {
        // TODO: Implement email service for confirming the updated information of a student
    }

    @RabbitListener(queues = "student.deleted")
    public void handleStudentDeletion(Events events) {
        // TODO: Implement email service for confirming the deletion of a student
    }
}
