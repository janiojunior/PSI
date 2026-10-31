import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatBadgeModule } from '@angular/material/badge';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatIconModule,
    MatButtonModule,
    MatBadgeModule
  ],
  templateUrl: './layout.component.html',
  styleUrl: './layout.component.scss'
})
export class LayoutComponent {
  private readonly router = inject(Router);

  get moduloAtual(): string {
    const url = this.router.url;
    if (url.startsWith('/municipios')) {
      return 'Municípios';
    }
    if (url.startsWith('/estados')) {
      return 'Estados';
    }
    if (url.startsWith('/pacientes')) {
      return 'Pacientes';
    }
    if (url.startsWith('/psicologos')) {
      return 'Psicólogos';
    }
    return 'Dashboard';
  }
}
