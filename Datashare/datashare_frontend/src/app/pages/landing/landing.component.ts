import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AppHeaderBar } from '../../components/app-header/app-header';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [AppHeaderBar],
  templateUrl: './landing.component.html',
  styleUrls: ['./landing.component.css']
})
export class LandingComponent {
  constructor(private router: Router) {}

  onUploadClick(): void {
    // US07 : l'upload est accessible sans compte
    this.router.navigate(['/upload']);
  }
}
