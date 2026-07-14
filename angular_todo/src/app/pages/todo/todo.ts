import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin, of } from 'rxjs';
import { delay, switchMap } from 'rxjs/operators';
import { MainTodoService } from '../../services/maintodo.service';
import { MainTodo } from '../../models/maintodo.model';
import { SubTodo } from '../../models/subtodo.model';
import { SubTodoService } from '../../services/subtodo.service';
@Component({
  selector: 'app-todo',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './todo.html',
  styleUrl: './todo.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Todo implements OnInit {

  private mainTodoService = inject(MainTodoService);
  private subTodoService = inject(SubTodoService);

  todos = signal<MainTodo[]>([]);
  errorMessage = signal('');
  editingTodoId = signal<number | null>(null);

  expandedTodoId = signal<number | null>(null);
  subtodos = signal<SubTodo[]>([]);
  subtodoLoading = signal(false);
  subtodoError = signal('');
  editingSubTodoId = signal<number | null>(null);
  newSubTodoTask = signal('');
  editingSubTodoTask = signal('');

  todoForm = new FormGroup({
    task: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(255)] }),
    description: new FormControl('', { nonNullable: true }),
    completed: new FormControl(false, { nonNullable: true }),
  });

  ngOnInit(): void {
    this.loadTodos();
  }

  loadTodos(): void {
    this.mainTodoService.getAllTodos().subscribe({
      next: (todos) => this.todos.set(todos),
      error: () => this.errorMessage.set('Failed to load todos.'),
    });
  }

  onSubmit(): void {
    this.errorMessage.set('');
    if (this.todoForm.invalid) return;

    const todoData = {
      task: this.todoForm.value.task,
      description: this.todoForm.value.description,
      completed: this.todoForm.value.completed,
    };

    const editId = this.editingTodoId();

    if (editId !== null) {
      this.mainTodoService.updateTodo(editId, todoData).subscribe({
        next: () => { this.resetForm(); this.loadTodos(); },
        error: (err) => this.errorMessage.set(typeof err.error === 'string' ? err.error : 'Failed to update todo.'),
      });
    } else {
      this.mainTodoService.createTodo(todoData).subscribe({
        next: () => { this.resetForm(); this.loadTodos(); },
        error: (err) => this.errorMessage.set(typeof err.error === 'string' ? err.error : 'Failed to create todo.'),
      });
    }
  }

  toggleComplete(todo: MainTodo): void {
    const newCompleted = !todo.completed;

    this.mainTodoService.updateTodo(todo.id, {
      task: todo.task,
      description: todo.description,
      completed: newCompleted,
    }).pipe(
      switchMap(() => {
        // Only cascade to subtodos when marking the main todo complete
        if (!newCompleted) {
          return of(null);
        }
        return this.subTodoService.getAllSubTodos(todo.id).pipe(
          switchMap((subs: SubTodo[]) => {
            const incomplete = subs.filter(s => !s.completed);
            if (incomplete.length === 0) {
              return of(null);
            }
            return forkJoin(
              incomplete.map(s =>
                this.subTodoService.updateSubTodo(s.task, true, todo.id, s.id)
              )
            );
          })
        );
      })
    ).subscribe({
      next: () => {
        this.loadTodos();
        if (this.expandedTodoId() === todo.id) {
          this.loadSubTodos(todo.id);
        }
      },
      error: () => this.errorMessage.set('Failed to update todo.'),
    });
  }

  editTodo(todo: MainTodo): void {
    this.editingTodoId.set(todo.id);
    this.todoForm.setValue({
      task: todo.task,
      description: todo.description ?? '',
      completed: todo.completed,
    });
  }

  deleteTodo(id: number): void {
    const previousTodos = this.todos();
    this.todos.set(previousTodos.filter(t => t.id !== id));

    this.mainTodoService.deleteTodo(id).subscribe({
      next: () => {
        this.loadTodos();
        this.subtodos.set([]);
        this.expandedTodoId.set(null);
      },
      error: (err) => {
        this.todos.set(previousTodos);
        this.errorMessage.set(`Failed to delete todo. Please try again. ${err}`);
      },
    });
  }

  cancelEdit(): void {
    this.resetForm();
  }

  toggleSubTodos(todo: MainTodo): void {
    if (this.expandedTodoId() === todo.id) {
      this.expandedTodoId.set(null);
      this.subtodos.set([]);
      this.cancelSubTodoEdit();
    } else {

      this.expandedTodoId.set(todo.id);
      this.loadSubTodos(todo.id);
      this.cancelSubTodoEdit();
    }
  }

  loadSubTodos(parentId: number): void {
    this.subtodoLoading.set(true);
    this.subtodoError.set('');
    this.subTodoService.getAllSubTodos(parentId).subscribe({
      next: (subs) => { this.subtodos.set(subs); this.subtodoLoading.set(false); },
      error: () => { this.subtodoError.set('Failed to load subtasks.'); this.subtodoLoading.set(false); },
    });
  }

  addSubTodo(parentId: number): void {
    const task = this.newSubTodoTask().trim();
    if (!task) return;
    this.subTodoService.createSubTodo(task, parentId).subscribe({
      next: () => { this.newSubTodoTask.set(''); this.loadSubTodos(parentId); },
      error: () => this.subtodoError.set('Failed to add subtask.'),
    });
  }

  startEditSubTodo(sub: SubTodo): void {
    this.editingSubTodoId.set(sub.id);
    this.editingSubTodoTask.set(sub.task);
  }

  saveSubTodoEdit(parentId: number, subId: number): void {
    const task = this.editingSubTodoTask().trim();
    if (!task) return;
    const current = this.subtodos().find(s => s.id === subId);
    this.subTodoService.updateSubTodo(task, current?.completed ?? false, parentId, subId).subscribe({
      next: () => { this.cancelSubTodoEdit(); this.loadSubTodos(parentId); },
      error: () => this.subtodoError.set('Failed to update subtask.'),
    });
  }

  cancelSubTodoEdit(): void {
    this.editingSubTodoId.set(null);
    this.editingSubTodoTask.set('');
  }

  toggleSubTodoComplete(parentId: number, sub: SubTodo): void {
    this.subTodoService.updateSubTodo(sub.task, !sub.completed, parentId, sub.id).subscribe({
      next: () => this.loadSubTodos(parentId),
      error: () => this.subtodoError.set('Failed to update subtask.'),
    });
  }

  deleteSubTodo(parentId: number, subId: number): void {
    const currentSubtodos = this.subtodos();
    this.subtodos.set(currentSubtodos.filter(s => s.id !== subId));
    this.subTodoService.deleteSubTodo(parentId, subId).subscribe({
      error: (err) => {
        this.subtodos.set(currentSubtodos);
        this.subtodoError.set(`Failed to delete subtask. Please try again. ${err}`);
      },
    });
  }

  private resetForm(): void {
    this.editingTodoId.set(null);
    this.todoForm.reset();
  }
}
