package com.example.taskmanager.service;

import com.example.taskmanager.dto.TaskRequest;
import com.example.taskmanager.dto.TaskResponse;
import com.example.taskmanager.entity.Task;
import com.example.taskmanager.entity.TaskStatus;
import com.example.taskmanager.exception.TaskNotFoundException;
import com.example.taskmanager.repository.TaskRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private Task buildTask(Long id, String title, String description, TaskStatus status) {
        Task task = new Task(title, description, status);
        task.setId(id);
        task.setCreatedAt(LocalDateTime.now());
        return task;
    }

    // ---------- createTask ----------

    @Test
    @DisplayName("createTask saves the task with the given status")
    void createTask_withStatus_savesAndReturnsTask() {
        TaskRequest request = new TaskRequest("Write report", "Quarterly report", TaskStatus.IN_PROGRESS);
        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> {
                    Task saved = invocation.getArgument(0);
                    saved.setId(1L);
                    return saved;
                });

        TaskResponse response = taskService.createTask(request);

        assertEquals(1L, response.id());
        assertEquals("Write report", response.title());
        assertEquals("Quarterly report", response.description());
        assertEquals(TaskStatus.IN_PROGRESS, response.status());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    @DisplayName("createTask defaults status to PENDING when none is provided")
    void createTask_withoutStatus_defaultsToPending() {
        TaskRequest request = new TaskRequest("Buy milk", null, null);
        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.createTask(request);

        assertEquals(TaskStatus.PENDING, response.status());

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(captor.capture());
        assertEquals("Buy milk", captor.getValue().getTitle());
        assertEquals(TaskStatus.PENDING, captor.getValue().getStatus());
    }

    // ---------- getAllTasks ----------

    @Test
    @DisplayName("getAllTasks returns every task as a response DTO")
    void getAllTasks_returnsAllTasks() {
        when(taskRepository.findAll()).thenReturn(List.of(
                buildTask(1L, "Task 1", "First", TaskStatus.PENDING),
                buildTask(2L, "Task 2", "Second", TaskStatus.COMPLETED)
        ));

        List<TaskResponse> responses = taskService.getAllTasks();

        assertEquals(2, responses.size());
        assertEquals("Task 1", responses.get(0).title());
        assertEquals("Task 2", responses.get(1).title());
        assertEquals(TaskStatus.COMPLETED, responses.get(1).status());
    }

    @Test
    @DisplayName("getAllTasks returns an empty list when there are no tasks")
    void getAllTasks_whenNoTasks_returnsEmptyList() {
        when(taskRepository.findAll()).thenReturn(List.of());

        List<TaskResponse> responses = taskService.getAllTasks();

        assertTrue(responses.isEmpty());
    }

    // ---------- getTaskById ----------

    @Test
    @DisplayName("getTaskById returns the task when it exists")
    void getTaskById_whenFound_returnsTask() {
        Task task = buildTask(1L, "Task 1", "First", TaskStatus.PENDING);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        TaskResponse response = taskService.getTaskById(1L);

        assertEquals(1L, response.id());
        assertEquals("Task 1", response.title());
        assertNotNull(response.createdAt());
    }

    @Test
    @DisplayName("getTaskById throws TaskNotFoundException when the task does not exist")
    void getTaskById_whenNotFound_throwsException() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        TaskNotFoundException ex = assertThrows(TaskNotFoundException.class,
                () -> taskService.getTaskById(99L));

        assertEquals("Task not found with id: 99", ex.getMessage());
    }

    // ---------- updateTask ----------

    @Test
    @DisplayName("updateTask changes title, description and status")
    void updateTask_whenFound_updatesAllFields() {
        Task existing = buildTask(1L, "Old title", "Old description", TaskStatus.PENDING);
        TaskRequest request = new TaskRequest("New title", "New description", TaskStatus.COMPLETED);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.updateTask(1L, request);

        assertEquals("New title", response.title());
        assertEquals("New description", response.description());
        assertEquals(TaskStatus.COMPLETED, response.status());
        verify(taskRepository).save(existing);
    }

    @Test
    @DisplayName("updateTask keeps the current status when none is provided")
    void updateTask_withoutStatus_keepsExistingStatus() {
        Task existing = buildTask(1L, "Old title", "Old description", TaskStatus.IN_PROGRESS);
        TaskRequest request = new TaskRequest("New title", "New description", null);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.updateTask(1L, request);

        assertEquals("New title", response.title());
        assertEquals(TaskStatus.IN_PROGRESS, response.status());
    }

    @Test
    @DisplayName("updateTask throws TaskNotFoundException and does not save when the task is missing")
    void updateTask_whenNotFound_throwsException() {
        TaskRequest request = new TaskRequest("Title", "Description", TaskStatus.PENDING);
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.updateTask(99L, request));

        verify(taskRepository, never()).save(any(Task.class));
    }

    // ---------- deleteTask ----------

    @Test
    @DisplayName("deleteTask deletes the task when it exists")
    void deleteTask_whenFound_deletesTask() {
        Task existing = buildTask(1L, "Task 1", "First", TaskStatus.PENDING);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existing));

        taskService.deleteTask(1L);

        verify(taskRepository).delete(existing);
    }

    @Test
    @DisplayName("deleteTask throws TaskNotFoundException and does not delete when the task is missing")
    void deleteTask_whenNotFound_throwsException() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.deleteTask(99L));

        verify(taskRepository, never()).delete(any(Task.class));
    }
}
