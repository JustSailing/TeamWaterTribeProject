import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../auth/auth.service';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Login {
  private authService = inject(AuthService);
  private router = inject(Router);

  loginForm = new FormGroup({
    username: new FormControl(''),
    password: new FormControl(''),
  });

  errorMessage = signal('');

  onSubmit(): void {
    this.errorMessage.set('');

    const username = this.loginForm.value.username ?? '';
    const password = this.loginForm.value.password ?? '';

    this.authService.login(username, password).subscribe({
      next: (token) => {
        // Store the JWT returned in the response body
        this.authService.setToken(token);
        this.router.navigate(['/home']);
      },
      error: (err) => {
        const message =
          typeof err.error === 'string'
            ? err.error
            : (err.error?.message ?? 'Invalid credentials. Please try again.');
        this.errorMessage.set(message);
      },
    });
  }
}