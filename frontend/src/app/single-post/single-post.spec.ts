import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SinglePost } from './single-post';

describe('SinglePost', () => {
  let component: SinglePost;
  let fixture: ComponentFixture<SinglePost>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SinglePost]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SinglePost);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
