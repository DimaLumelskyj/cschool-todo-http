package pl.connectis.todo.repository;

import pl.connectis.todo.domain.Task;

import java.util.List;

public interface TaskRepository {

    List<Task> getAll();

    Task get(long id);

    Task add(Task task);
}
