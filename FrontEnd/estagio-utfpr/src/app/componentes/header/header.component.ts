import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router, NavigationEnd, Event } from '@angular/router';
import { filter, Subscription } from 'rxjs';

@Component({
  selector: 'app-header',
  standalone: true,
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit, OnDestroy {
  activeRoute: string = '';
  private routerSubscription!: Subscription;

  constructor(private router: Router) {}

  ngOnInit(): void {
    this.routerSubscription = this.router.events
      .pipe(
        filter((event: Event): event is NavigationEnd => event instanceof NavigationEnd)
      )
      .subscribe((event: NavigationEnd) => {
        this.updateActiveRoute(event.url);
      });

    this.updateActiveRoute(this.router.url);
  }

  ngOnDestroy(): void {
    if (this.routerSubscription) {
      this.routerSubscription.unsubscribe();
    }
  }

  private updateActiveRoute(url: string): void {
    if (url.includes('estudante')) {
      this.activeRoute = 'estudante';
    } else if (url.includes('orientador')) {
      this.activeRoute = 'orientador';
    } else if (url.includes('prae')) {
      this.activeRoute = 'prae';
    } else {
      this.activeRoute = '';
    }
  }

  navigateToEstudante(): void {
    this.router.navigate(['/estudante']);
  }

  navigateToOrientador(): void {
    this.router.navigate(['/orientador']);
  }
  
  navigateToPrae(): void {
    this.router.navigate(['/prae']);
  }

  isActive(route: string): boolean {
    return this.activeRoute === route;
  }
}