import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { SignupComponent } from './signup/signup.component';
import { HomeComponent } from './home/home.component';
import { authGuard } from './guard/auth.guard';
import { ProfilePageComponent } from './profile-page/profile-page.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { ShoppingCartComponent } from './shopping-cart/shopping-cart.component';
import { SearchComponent } from './search/search.component';

const routes: Routes = [
  { path: '*', redirectTo: 'home', pathMatch: 'full' },
  { path: 'login', component: LoginComponent, canActivate: [authGuard] },
  { path: 'register', component: SignupComponent, canActivate: [authGuard] },
  { path: 'home', component: HomeComponent, canActivate: [authGuard] },
  {
    path: 'search',
    component: SearchComponent,
    canActivate: [authGuard],
  },
  {
    path: 'profile',
    component: ProfilePageComponent,
    canActivate: [authGuard],
  },
  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [authGuard],
  },
  {
    path: 'shopcart',
    component: ShoppingCartComponent,
    canActivate: [authGuard],
  },
  { path: '**', redirectTo: 'home', pathMatch: 'full' },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
