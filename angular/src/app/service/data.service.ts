import { Injectable } from '@angular/core';
import { BehaviorSubject, Subject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class DataService {
  //eslint-disable-next-line
  private dataSource = new BehaviorSubject<any>(null);
  data$ = this.dataSource.asObservable();

  private idSource = new Subject<string>();
  ids$ = this.idSource.asObservable();

  private deleteImageSource = new BehaviorSubject<number>(0);
  deleteImage$ = this.deleteImageSource.asObservable();

  private dashboardSource = new BehaviorSubject<boolean>(false);
  dashboard$ = this.dashboardSource.asObservable();

  //eslint-disable-next-line
  sendData(data: any): void {
    this.dataSource.next(data);
  }

  sendProductId(data: string): void {
    this.idSource.next(data);
  }

  changeDeleteIndex(data: number): void {
    this.deleteImageSource.next(data);
  }

  updateDashboard(data: boolean): void {
    this.dashboardSource.next(data);
  }
}
