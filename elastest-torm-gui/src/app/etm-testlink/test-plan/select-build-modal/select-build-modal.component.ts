import { Component, OnInit, Inject } from '@angular/core';
import { Router } from '@angular/router';
import { TestPlanModel } from '../../models/test-plan-model';
import { BuildModel } from '../../models/build-model';
import { MD_DIALOG_DATA } from '@angular/material';
import { TestLinkService } from '../../testlink.service';
import { TLTestCaseModel } from '../../models/test-case-model';

@Component({
  selector: 'app-select-build-modal',
  templateUrl: './select-build-modal.component.html',
  styleUrls: ['./select-build-modal.component.scss'],
})
export class SelectBuildModalComponent implements OnInit {
  selectedBuild: BuildModel;
  testPlan: TestPlanModel;
  builds: BuildModel[];
  testProjectId: string | number;
  testPlanCases: TLTestCaseModel[];

  ready: boolean = false;
  fail: boolean = false;

  constructor(private router: Router, private testLinkService: TestLinkService, @Inject(MD_DIALOG_DATA) public data: any) {
    this.init();
  }

  ngOnInit() {}

  init(): void {
    this.testProjectId = this.data.testProjectId;
    this.testPlan = this.data.testPlan;
    if (this.data.builds) {
      this.builds = this.data.builds;
      this.setReady();
    } else {
      this.testLinkService.getPlanBuilds(this.testPlan).subscribe(
        (builds: BuildModel[]) => {
          this.builds = builds;
          this.setReady();
        },
        (error: Error) => console.log(error),
      );
    }
  }

  setReady(): void {
    if (this.testPlan && this.builds !== undefined && this.builds !== null && this.testProjectId) {
      this.testLinkService.getPlanTestCases(this.testPlan).subscribe(
        (testCases: TLTestCaseModel[]) => {
          this.testPlanCases = testCases;
          this.ready = true;
        },
        (error) => {
          console.log(error);
          this.fail = true;
        },
      );
    } else {
      this.fail = true;
    }
  }

  runTestPlan(): void {
    this.router.navigate([
      '/testlink/projects',
      this.testProjectId,
      'plans',
      this.testPlan.id,
      'builds',
      this.selectedBuild.id,
      'exec',
      'new',
    ]);
  }
}
