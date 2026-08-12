import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ModalComponent } from './modal';
import { EventEmitter } from '@angular/core';


describe('ModalComponent', () => {
  let component: ModalComponent;
  let fixture: ComponentFixture<ModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalComponent] // standalone
    }).compileComponents();

    fixture = TestBed.createComponent(ModalComponent);
    component = fixture.componentInstance;
  });

  it('should display the modal when visible is true', () => {
    component.visible = true;
    fixture.detectChanges();

    const backdrop = fixture.nativeElement.querySelector('.modal-backdrop');
    expect(backdrop).toBeTruthy();
  });

  it('should not display the modal when visible is false', () => {
    component.visible = false;
    fixture.detectChanges();

    const backdrop = fixture.nativeElement.querySelector('.modal-backdrop');
    expect(backdrop).toBeNull();
  });

  it('should emit close event', () => {
    component.visible = true;
    fixture.detectChanges();

    let closeEmitted = false;
    component.close.subscribe(() => {
      closeEmitted = true;
    });

    const btn = fixture.nativeElement.querySelector('button:first-child');
    btn.click();

    expect(closeEmitted).toBe(true);
  });

  it('should emit confirm event', () => {
    component.visible = true;
    fixture.detectChanges();

    let confirmEmitted = false;
    component.confirm.subscribe(() => {
      confirmEmitted = true;
    });

    const btn = fixture.nativeElement.querySelector('button:last-child');
    btn.click();

    expect(confirmEmitted).toBe(true);
  });
});

