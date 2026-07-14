import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MainTodo } from '../models/maintodo.model';

@Injectable({
  providedIn: 'root'
})
export class MainTodoService {

  private apiUrl = 'http://localhost:8080/api/main-todos';

  constructor(private http: HttpClient) {}

  getAllTodos(): Observable<MainTodo[]> {
    return this.http.get<MainTodo[]>(this.apiUrl);
  }

  createTodo(todoData: Partial<MainTodo>): Observable<MainTodo> {
    return this.http.post<MainTodo>(this.apiUrl, todoData);
  }

  updateTodo(todoId: number, todoData: Partial<MainTodo>): Observable<MainTodo> {
    return this.http.put<MainTodo>(`${this.apiUrl}/${todoId}`, todoData);
  }

  deleteTodo(todoId: number): Observable<string> {
    return this.http.delete(`${this.apiUrl}/${todoId}`, { responseType: 'text' });
  }
}
