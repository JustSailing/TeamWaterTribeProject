import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router, RouterLink } from '@angular/router';

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrl: './register.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Register {
  // Inject HttpClient to make HTTP requests to the backend
  private http = inject(HttpClient);
  private router = inject(Router);

  // A FormGroup is a collection of FormControls that represents the entire form.
  // It tracks the value and validation state of each child control as a single unit.
  // In the template, we bind it to a <form> element with [formGroup]="registerForm",
  // which is what enables (ngSubmit) to fire when the form is submitted.
  // Each FormControl inside holds the value for one input field (initialized to '').
  registerForm = new FormGroup({
    email: new FormControl(''),
    username: new FormControl(''),
    password: new FormControl(''),
  });

  // Signals to hold feedback messages displayed in the template
  successMessage = signal('');
  errorMessage = signal('');

  // Called when the form is submitted via (ngSubmit)
  onSubmit(): void {
    // Clear any previous messages before a new attempt
    this.successMessage.set('');
    this.errorMessage.set('');

    // Build the request body from the form values
    const body = {
      email: this.registerForm.value.email,
      username: this.registerForm.value.username,
      password: this.registerForm.value.password,
    };

    // POST to the backend registration endpoint and handle the response
    // Arguments:
    //   1st arg: the URL of the backend endpoint
    //   2nd arg: the request body, sent as JSON by default
    //   3rd arg: options object —
    //     observe: 'response' gives us the full HttpResponse (including status code),
    //              rather than just the parsed body
    //     responseType: 'text' tells Angular to treat the response body as plain text
    //                   instead of attempting to parse it as JSON
    this.http.post('http://localhost:8080/register', body, { responseType: 'text' }).subscribe({
      next: () => {
        this.successMessage.set('Registration successful!');
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 1500);
      },
      error: (err) => {
        const message = typeof err.error === 'string' && err.error
          ? err.error
          : 'An unexpected error occurred. Please try again.';
        this.errorMessage.set(message);
      },
    });
  }
}