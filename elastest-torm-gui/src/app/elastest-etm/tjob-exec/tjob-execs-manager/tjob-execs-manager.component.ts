import { ActivatedRoute, Router } from '@angular/router';
import { TdDialogService } from '@covalent/core/dialogs/services/dialog.service';
import { IConfirmConfig, TdDataTableSortingOrder } from '@covalent/core';
import { TJobExecModel } from '../tjobExec-model';
import { TJobExecService } from '../tjobExec.service';
import { TitlesService } from '../../../shared/services/titles.service';
import { Component, OnInit, ViewContainerRef } from '@angular/core';
import { MdDialog } from '@angular/material';

@Component({
  selector: 'app-tjob-execs-manager',
  templateUrl: './tjob-execs-manager.component.html',
  styleUrls: ['./tjob-execs-manager.component.scss']
})
export class TJobExecsManagerComponent implements OnInit {

  allTJobExecs: TJobExecModel[] = [];
  tJobExecsFinished: TJobExecModel[] = [];
  tJobExecsRunning: TJobExecModel[] = [];

  defaultRefreshText: string = 'Refresh';
  refreshText: string = this.defaultRefreshText;
  deletingInProgress: boolean = false;

  // TJob Exec Data
  tJobExecColumns: any[] = [
    { name: 'id', label: 'Id' },
    { name: 'tJob.name', label: 'TJob' },
    { name: 'result', label: 'Result' },
    { name: 'startDate', label: 'Start Date' },
    { name: 'endDate', label: 'End Date' },
    { name: 'lastExecutionDate', label: 'Last Execution' },
    // { name: 'duration', label: 'Duration' },
    // { name: 'sutExecution', label: 'Sut Execution' },
    // { name: 'error', label: 'Error' },
    // { name: 'monitoringIndex', label: 'Log Index' },
    { name: 'options', label: 'Options' },
  ];

  constructor(
    private titlesService: TitlesService, private tJobExecService: TJobExecService,
    private route: ActivatedRoute, private router: Router,
    private _dialogService: TdDialogService, private _viewContainerRef: ViewContainerRef,
    public dialog: MdDialog,
  ) { }

  ngOnInit() {
    this.titlesService.setHeadTitle('Dashboard');
    this.loadTJobExecs();
  }

  loadTJobExecs(refreshText: string = ''): void {
    this.refreshText = refreshText;
    this.tJobExecService.getAllTJobExecs()
      .subscribe((tJobExecs: TJobExecModel[]) => {
        this.tJobExecsFinished = [];
        this.tJobExecsRunning = [];

        this.allTJobExecs = tJobExecs;
        tJobExecs = tJobExecs.reverse(); // To sort Descending
        tJobExecs.map(
          (tJobExec: TJobExecModel) => {
            if (tJobExec.finished()) {
              tJobExec['lastExecutionDate'] = tJobExec.endDate;
              this.tJobExecsFinished.push(tJobExec);
            } else {
              this.tJobExecsRunning.push(tJobExec);
            }
          }
        );
        this.refreshText = this.defaultRefreshText;
      });
  }


  deleteTJobExec(tJobExec: TJobExecModel) {
    let iConfirmConfig: IConfirmConfig = {
      message: 'TJob Execution ' + tJobExec.id + ' will be deleted, do you want to continue?',
      disableClose: false,
      viewContainerRef: this._viewContainerRef,
      title: 'Confirm',
      cancelButton: 'Cancel',
      acceptButton: 'Yes, delete',
    };
    this._dialogService.openConfirm(iConfirmConfig).afterClosed().subscribe((accept: boolean) => {
      if (accept) {
        this.deletingInProgress = true;
        this.tJobExecService.deleteTJobExecution(tJobExec.tJob, tJobExec).subscribe(
          (exec: TJobExecModel) => {
            this.deletingInProgress = false;
            this.tJobExecService.popupService.openSnackBar('TJob Execution Nº' + tJobExec.id + ' has been removed successfully!');
            this.loadTJobExecs();
          },
          (error) => {
            this.deletingInProgress = true;
            this.tJobExecService.popupService.openSnackBar('TJob Execution could not be deleted');
          }
        );
      }
    });
  }

  viewTJobExec(tJobExec: TJobExecModel) {
    this.router.navigate(['/projects', tJobExec.tJob.project.id, 'tjob', tJobExec.tJob.id, 'tjob-exec', tJobExec.id]);
  }

  viewInLogAnalyzer(tJobExec: TJobExecModel): void {
    this.router.navigate(
      ['/loganalyzer'],
      { queryParams: { tjob: tJobExec.tJob.id, exec: tJobExec.id } }
    );
  }
}
