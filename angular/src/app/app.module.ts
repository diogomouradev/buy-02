import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { CheckboxModule } from 'primeng/checkbox';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { LoginComponent } from './login/login.component';
import { SignupComponent } from './signup/signup.component';
import { ShoppingCartComponent } from './shopping-cart/shopping-cart.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { SearchComponent } from './search/search.component';
import { OrderCardComponent } from './order-card/order-card.component';
import { StyleClassModule } from 'primeng/styleclass';
import { ButtonModule } from 'primeng/button';
import { RippleModule } from 'primeng/ripple';
import { InputTextModule } from 'primeng/inputtext';
import { HomeComponent } from './home/home.component';
import { HttpClientModule } from '@angular/common/http';
import { NavbarComponent } from './navbar/navbar.component';
import { AddProductComponent } from './add-product/add-product.component';
import { ProductContainerComponent } from './product-container/product-container.component';
import { ProductCardModalComponent } from './product-container/product-card-modal/product-card-modal.component';
import { ProductCardComponent } from './product-container/product-card/product-card.component';
import { CookieService } from 'ngx-cookie-service';
import { CarouselModule } from 'primeng/carousel';
import { ProfilePageComponent } from './profile-page/profile-page.component';
import { StateService } from './service/state.service';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { FileUploadModule } from 'primeng/fileupload';
import { GalleriaModule } from 'primeng/galleria';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { InputNumberModule } from 'primeng/inputnumber';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatBadgeModule } from '@angular/material/badge';

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    SignupComponent,
    HomeComponent,
    NavbarComponent,
    AddProductComponent,
    ProductContainerComponent,
    ProductCardModalComponent,
    ProductCardComponent,
    ProfilePageComponent,
    ShoppingCartComponent,
    DashboardComponent,
    SearchComponent,
    OrderCardComponent,
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    AppRoutingModule,
    CheckboxModule,
    StyleClassModule,
    ButtonModule,
    RippleModule,
    InputTextModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    CarouselModule,
    MatIconModule,
    MatTabsModule,
    FileUploadModule,
    GalleriaModule,
    InputTextareaModule,
    InputNumberModule,
    MatTooltipModule,
    MatBadgeModule,
  ],
  providers: [CookieService],
  bootstrap: [AppComponent],
})
export class AppModule {
  constructor(private stateService: StateService) {
    this.stateService.initialize();
  }
}
