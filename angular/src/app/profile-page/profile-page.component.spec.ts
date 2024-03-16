import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProfilePageComponent } from './profile-page.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { AuthService } from '../service/auth.service';
import { NavbarComponent } from '../navbar/navbar.component';
import { CookieService } from 'ngx-cookie-service';
import { FormStateService } from '../service/form-state.service';
import { MediaService } from '../service/media.service';
import { UserService } from '../service/user.service';
import { BehaviorSubject, of, Subject } from 'rxjs';
import { User } from '../interfaces/user';
import { Media } from '../interfaces/media';
import { FileSelectEvent } from 'primeng/fileupload';

describe('ProfilePageComponent', () => {
  let component: ProfilePageComponent;
  let fixture: ComponentFixture<ProfilePageComponent>;
  let authService: AuthService;
  let mediaService: MediaService;
  //eslint-disable-next-line
  let formStateService: FormStateService;
  //eslint-disable-next-line
  let userService: UserService;
  let cookieService: CookieService;

  const GMockUser: User = {
    id: '123',
    name: 'tester',
    email: 'tester@gmail.com',
    role: 'SELLER',
  };
  const GMockMedia: Media = {
    id: '1234',
    image: 'imagestring',
    productId: '',
    userId: '123',
    mimeType: 'image/png',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ProfilePageComponent, NavbarComponent],
      imports: [HttpClientTestingModule],
      providers: [
        AuthService,
        MediaService,
        FormStateService,
        UserService,
        CookieService,
      ],
    });
    fixture = TestBed.createComponent(ProfilePageComponent);
    component = fixture.componentInstance;

    authService = TestBed.inject(AuthService);
    mediaService = TestBed.inject(MediaService);
    formStateService = TestBed.inject(FormStateService);
    userService = TestBed.inject(UserService);
    cookieService = TestBed.inject(CookieService);

    component.user$ = new Subject<User>();
    component.avatar$ = new BehaviorSubject(component.placeholder);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should not initialize if there is no cookie', () => {
    spyOn(cookieService, 'get').and.returnValue('');
    fixture.detectChanges();
    expect(component).toBeTruthy();
    expect(cookieService.get).toHaveBeenCalledWith('buy-01');
    expect(component.formOpen).toBeFalsy();
  });

  it('should get user authentication and avatar', () => {
    const mockUser: User = {
      id: '123',
      name: 'tester',
      email: 'tester@gmail.com',
      role: 'SELLER',
    };
    const mockMedia: Media = {
      id: '1234',
      image: 'imagestring',
      productId: '',
      userId: '123',
      mimeType: 'image/png',
    };

    spyOn(authService, 'getAuth').and.returnValue(of(mockUser));
    spyOn(mediaService, 'getAvatar').and.returnValue(of(mockMedia));
    spyOn(mediaService, 'formatMedia').and.returnValue(
      'data:' + mockMedia.mimeType + ';base64,' + mockMedia.image,
    );

    //eslint-disable-next-line
    (component as any).getAuthAndAvatar();

    expect(component.currentUser).toEqual(mockUser);
  });

  it('should get user authentication and placeholder avatar if user has no avatar', () => {
    const mockUser: User = {
      id: '123',
      name: 'tester',
      email: 'tester@gmail.com',
      role: 'SELLER',
    };

    spyOn(authService, 'getAuth').and.returnValue(of(mockUser));

    //eslint-disable-next-line
    component.getAuthAndAvatar();

    expect(component.currentUser).toEqual(mockUser);
  });

  it(
    'should react to image and username changes',
    () => {
      const mockUser = GMockUser;
      const userService = TestBed.inject(UserService);
      const mediaService = TestBed.inject(MediaService);

      spyOn(cookieService, 'get').and.returnValue(
        'buy-01',
      );

      const getAuthAndAvatarSpy = spyOn(component, 'getAuthAndAvatar');

      mediaService.updateImageAdded(GMockMedia);

      fixture.detectChanges();

      getAuthAndAvatarSpy.calls.reset();

      userService.updateUsernameAdded(mockUser);
      fixture.detectChanges();

      expect(getAuthAndAvatarSpy).not.toHaveBeenCalled();

      component.user$.subscribe((user) => {
        expect(user).toEqual(mockUser);
      });
    },
  );

  it('should set fileSelected and filename when a file is selected', () => {
    const file: File = new File([''], 'test.png', { type: 'image/png' });
    const event = { files: [file] } as FileSelectEvent;

    component.onFileSelected(event);

    expect(component.fileSelected).toEqual(file);
    expect(component.filename).toBe('test.png');
  });

  it('should set fileSelected to null when no file is selected', () => {
    const event = { files: [] } as unknown as FileSelectEvent;

    component.onFileSelected(event);

    expect(component.fileSelected).toBeNull();
  });

  it('should upload the avatar', () => {
    const mockAvatarData: FormData = new FormData();
    const mockUserData = GMockUser;
    const mockMediaData = GMockMedia;

    const uploadAvatarSpy = spyOn(mediaService, 'uploadAvatar').and.returnValue(
      of(mockMediaData),
    );

    component.currentUser = mockUserData;
    component.fileSelected = new File([''], 'test.png', { type: 'image/png' });
    component.filename = 'test.png';

    component.submitAvatar();

    fixture.detectChanges();
    expect(uploadAvatarSpy).toHaveBeenCalledWith('123', mockAvatarData);
    expect(component.currentUser.avatar).toBe('1234');
  });

  it('should convert a File to a Blob', () => {
    const file: File = new File(['test data'], 'test.txt', {
      type: 'text/plain',
    });
    const blob = component.fileToBlob(file);

    expect(blob instanceof Blob).toBe(true);
    expect(blob.type).toBe('text/plain');
  });

  it('should initialize userUpdateForm with the expected controls', () => {
    expect(component.userUpdateForm.get('name')).not.toBeNull();
    expect(component.userUpdateForm.get('email')).not.toBeNull();
    expect(component.userUpdateForm.get('password')).not.toBeNull();
    expect(component.userUpdateForm.get('confirmPassword')).not.toBeNull();
  });

  it('should open the user profile form', () => {
    component.openForm('profile');
    fixture.detectChanges();
    expect(component.profileFormOpen).toBe(true);
  });

  it('should validate the user update form', () => {
    component.userUpdateForm.setValue({
      name: 'Valid Name',
      email: 'valid@email.com',
      password: 'validPassword',
      confirmPassword: 'validPassword',
    });
    component.onValidate();
    expect(component.formValid).toBe(true);

    component.userUpdateForm.setValue({
      name: 'me',
      email: 'invalid.email',
      password: 'short',
      confirmPassword: 'mismatch',
    });
    component.onValidate();
    expect(component.formValid).toBe(false);
  });

  it('should open profileform and set buttonClicked and formOpen to true', () => {
    const component =
      TestBed.createComponent(ProfilePageComponent).componentInstance;

    component.openForm('profile');
    fixture.detectChanges();
    formStateService.formOpen$.subscribe((formOpen) => {
      expect(formOpen).toBe(true);
    });

    expect(component.profileFormOpen).toBe(true);
    expect(component.buttonClicked).toBe(true);
  });

  it('should open updateAvatarForm and set buttonClicked and formOpen to true', () => {
    const component =
      TestBed.createComponent(ProfilePageComponent).componentInstance;

    component.openForm('avatar');
    fixture.detectChanges();
    formStateService.formOpen$.subscribe((formOpen) => {
      expect(formOpen).toBe(true);
    });

    expect(component.updateAvatarFormOpen).toBe(true);
    expect(component.buttonClicked).toBe(true);
  });

  it('should open deleteAvatarForm and set buttonClicked and formOpen to true', () => {
    const component =
      TestBed.createComponent(ProfilePageComponent).componentInstance;

    component.openForm('deleteAvatar');
    fixture.detectChanges();
    formStateService.formOpen$.subscribe((formOpen) => {
      expect(formOpen).toBe(true);
    });

    expect(component.deleteAvatarFormOpen).toBe(true);
    expect(component.buttonClicked).toBe(true);
  });

  it('should open deleteUserForm and set buttonClicked and formOpen to true', () => {
    const component =
      TestBed.createComponent(ProfilePageComponent).componentInstance;

    component.openForm('deleteUser');
    fixture.detectChanges();
    formStateService.formOpen$.subscribe((formOpen) => {
      expect(formOpen).toBe(true);
    });

    expect(component.deleteUserFormOpen).toBe(true);
    expect(component.buttonClicked).toBe(true);
  });

  it('should update formValid to false for invalidForm', () => {
    const component =
      TestBed.createComponent(ProfilePageComponent).componentInstance;
    component.userUpdateForm.setValue({
      name: 'Jo',
      email: 'john@example',
      password: null,
      confirmPassword: null,
    });

    component.onValidate();

    expect(component.formValid).toBe(false);
  });

  it('should update formValid to true for validForm', () => {
    const component =
      TestBed.createComponent(ProfilePageComponent).componentInstance;
    component.userUpdateForm.setValue({
      name: 'John',
      email: 'john@example.com',
      password: null,
      confirmPassword: null,
    });

    component.onValidate();

    expect(component.formValid).toBe(true);
  });

  it('should hide the modal and reset state variables', () => {
    const component =
      TestBed.createComponent(ProfilePageComponent).componentInstance;
    component.profileFormOpen = true;
    component.buttonClicked = true;

    component.hideModal();
    formStateService.formOpen$.subscribe((formOpen) => {
      expect(formOpen).toBe(false);
    });
    expect(component.profileFormOpen).toBe(false);
    expect(component.deleteUserFormOpen).toBe(false);
    expect(component.deleteAvatarFormOpen).toBe(false);
    expect(component.updateAvatarFormOpen).toBe(false);
  });

  it('should submit the form and update the user', () => {
    const component =
      TestBed.createComponent(ProfilePageComponent).componentInstance;
    const userService = TestBed.inject(UserService);
    spyOn(userService, 'updateUser').and.callThrough();
    spyOn(component, 'hideModal').and.callThrough();

    component.currentUser = GMockUser;
    component.userUpdateForm.setValue({
      name: 'John',
      email: 'john@example.com',
      password: 'test',
      confirmPassword: 'test',
    });

    component.onSubmit();

    formStateService.formOpen$.subscribe((formOpen) => {
      expect(formOpen).toBe(false);
    });

    expect(userService.updateUser).toHaveBeenCalledWith(
      {
        name: 'John',
        email: 'john@example.com',
        password: 'test',
      },
      component.currentUser.id,
    );

    expect(component.userUpdateForm.value.name).toBe('John');
    expect(component.userUpdateForm.value.email).toBe('john@example.com');
    expect(component.userUpdateForm.value.password).toEqual('test');
    expect(component.userUpdateForm.value.confirmPassword).toEqual('test');

    expect(component.hideModal).toHaveBeenCalled();
  });

  it('should delete the avatar', () => {
    const component =
      TestBed.createComponent(ProfilePageComponent).componentInstance;
    const mediaService = TestBed.inject(MediaService);
    const mediaServiceSpy = spyOn(mediaService, 'deleteAvatar').and.returnValue(
      of({}),
    );

    component.deleteAvatar();

    expect(mediaServiceSpy).toHaveBeenCalledWith(component.currentUser.id);
    expect(mediaServiceSpy).toHaveBeenCalledOnceWith(component.currentUser.id);
  });

  it('should delete the user', () => {
    const component =
      TestBed.createComponent(ProfilePageComponent).componentInstance;
    const userService = TestBed.inject(UserService);
    const userServiceSpy = spyOn(userService, 'deleteUser').and.returnValue(
      of({}),
    );
    const logoutSpy = spyOn(userService, 'logout');

    component.deleteUser();

    expect(userServiceSpy).toHaveBeenCalledWith(component.currentUser.id);
    expect(userServiceSpy).toHaveBeenCalledOnceWith(component.currentUser.id);
    expect(logoutSpy).toHaveBeenCalledOnceWith();
  });
});
