import { SubTodo } from "./subtodo.model";

export interface MainTodo {
  id: number;
  task: string;
  description: string;
  completed: boolean;
  expanded?: boolean;
  subtodos?: SubTodo[]
}