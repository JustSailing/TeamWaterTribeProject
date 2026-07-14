import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SubTodo } from '../models/subtodo.model';

@Injectable({
  providedIn: 'root'
})
export class SubTodoService {

  private readonly apiUrl = 'http://localhost:8080/api/main-todos';
  private http = inject(HttpClient);

  getAllSubTodos(parent_id: number): Observable<SubTodo[]> {
    return this.http.get<SubTodo[]>(`${this.apiUrl}/${parent_id}/subtasks`);
  }

  createSubTodo(task: string, parent_id: number): Observable<SubTodo> {
    return this.http.post<SubTodo>(`${this.apiUrl}/${parent_id}/subtasks`, { task });
  }

  updateSubTodo(task: string, completed: boolean, parent_id: number, id: number): Observable<SubTodo> {
    return this.http.put<SubTodo>(`${this.apiUrl}/${parent_id}/subtasks/${id}`, { task, completed });
  }

  deleteSubTodo(parent_id: number, id: number): Observable<string> {
    return this.http.delete(`${this.apiUrl}/${parent_id}/subtasks/${id}`, { responseType: 'text' });
  }
}
