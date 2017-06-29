import { Router } from '@angular/router';
import { SutModel } from '../../sut/sut-model';
import { SutService } from '../../sut/sut.service';
import { TJobModel } from '../../tjob/tjob-model';
import { TJobService } from '../../tjob/tjob.service';
import { ProjectModel } from '../project-model';
import { ProjectService } from '../project.service';
import { Title } from '@angular/platform-browser';
import {
  IPageChangeEvent,
  ITdDataTableRowClickEvent,
  ITdDataTableSortChangeEvent,
  TdDataTableService,
  TdDataTableSortingOrder,
  TdDialogService,
  IConfirmConfig,
} from '@covalent/core';
import { AfterViewInit, Component, OnInit, ViewContainerRef } from '@angular/core';

@Component({
  selector: 'etm-projects-manager',
  templateUrl: './projects-manager.component.html',
  styleUrls: ['./projects-manager.component.scss']
})
export class ProjectsManagerComponent implements OnInit, AfterViewInit {

  // Project data
  projectColumns: any[] = [
    { name: 'id', label: 'Id' },
    { name: 'name', label: 'Project' },
    { name: 'options', label: 'Options' },
  ];

  projectData: ProjectModel[] = [];

  filteredData: any[] = [];
  filteredTotal: number = 0;
  searchTerm: string = '';
  fromRow: number = 1;
  currentPage: number = 1;
  pageSize: number = 5;
  sortBy: string = 'name';
  sortOrder: TdDataTableSortingOrder = TdDataTableSortingOrder.Ascending;

  projectChildsActived: boolean = false;
  projectSelected: ProjectModel = undefined;

  // SuT Data
  sutColumns: any[] = [
    { name: 'id', label: 'Id' },
    { name: 'name', label: 'Name' },
    { name: 'specification', label: 'GitHub App Repo' },
    { name: 'description', label: 'Description' },
    { name: 'options', label: 'Options' },
  ];

  sutData: SutModel[] = [];

  // TJob Data
  tjobColumns: any[] = [
    { name: 'id', label: 'Id' },
    { name: 'name', label: 'Name' },
    { name: 'imageName', label: 'Image Name' },
    { name: 'sut.id', label: 'Sut' },
    { name: 'options', label: 'Options' },
  ];

  tjobData: TJobModel[] = [];

  // Create TJob
  newTJobName: string = '';
  newTJobImage: string = '';
  sutEmpty: SutModel = new SutModel();
  selectedSut: SutModel = this.sutEmpty;

  // Create SuT
  newSutName: string = '';
  newSutRepo: string = '';
  newSutDesc: string = '';



  constructor(private _titleService: Title,
    private _dataTableService: TdDataTableService, private projectService: ProjectService, private router: Router,
    private _dialogService: TdDialogService, private _viewContainerRef: ViewContainerRef,
    private tJobService: TJobService, private sutService: SutService) { }

  ngOnInit() {
    this.loadProjects();
  }

  loadProjects() {
    this.projectService.getProjects()
      .subscribe(
      (projects) => this.prepareDataTable(projects),
    );
  }

  reloadProjects() {
    this.projectService.getProjects()
      .subscribe(
      (projects) => {
        this.prepareDataTable(projects);
        this.projectSelected = this.projectData.find((project) => project.id === this.projectSelected.id);
        this.showProjectChildsByProject(this.projectSelected);
      },
    );
  }

  prepareDataTable(projects: ProjectModel[]) {
    this.projectData = projects;
    this.filteredData = this.projectData;
    this.filteredTotal = this.projectData.length;
    this.filter();
  }

  ngAfterViewInit(): void {
    this._titleService.setTitle('Product Stats');
    this.filter();
  }

  sort(sortEvent: ITdDataTableSortChangeEvent): void {
    this.sortBy = sortEvent.name;
    this.sortOrder = sortEvent.order;
    this.filter();
  }

  search(searchTerm: string): void {
    this.searchTerm = searchTerm;
    this.filter();
  }

  page(pagingEvent: IPageChangeEvent): void {
    this.fromRow = pagingEvent.fromRow;
    this.currentPage = pagingEvent.page;
    this.pageSize = pagingEvent.pageSize;
    this.filter();
  }

  filter(): void {
    let newData: any[] = this.projectData;
    newData = this._dataTableService.filterData(newData, this.searchTerm, true);
    this.filteredTotal = newData.length;
    newData = this._dataTableService.sortData(newData, this.sortBy, this.sortOrder);
    newData = this._dataTableService.pageData(newData, this.fromRow, this.currentPage * this.pageSize);
    this.filteredData = newData;
  }

  editProject(project: ProjectModel) {
    console.log(project);
    this.router.navigate(['/projects-management/edit', project.id]);
  }

  deleteProject(project: ProjectModel) {
    let iConfirmConfig: IConfirmConfig = {
      message: 'Project ' + project.id + ' will be deleted, do you want to continue?',
      disableClose: false, // defaults to false
      viewContainerRef: this._viewContainerRef,
      title: 'Confirm',
      cancelButton: 'Cancel',
      acceptButton: 'Yes, delete',
    };
    this._dialogService.openConfirm(iConfirmConfig).afterClosed().subscribe((accept: boolean) => {
      if (accept) {
        this.projectService.deleteProject(project).subscribe(
          (project) => this.loadProjects(),
          (error) => console.log(error)
        );
      }
    });
  }

  showProjectChilds(event: ITdDataTableRowClickEvent) {
    this.showProjectChildsByProject(event.row);
  }

  showProjectChildsByProject(project: ProjectModel) {
    this.projectChildsActived = true;
    this.projectSelected = project;
    this.sutData = project.suts;
    this.tjobData = project.tjobs;
    this.clearTJobForm();
  }

  // TJobs functions

  createTJob() {
    let tJob: TJobModel = new TJobModel();
    tJob.name = this.newTJobName;
    tJob.imageName = this.newTJobImage;
    if (this.selectedSut.id > 0) {
      tJob.sut = this.selectedSut;
    }
    tJob.project = this.projectSelected;

    this.tJobService.createTJob(tJob).subscribe(
      (tJob) => {
        this.reloadProjects();
        this.clearTJobForm();
      },
      (error) => console.log(error),
    );
  }

  clearTJobForm() {
    this.newTJobName = ''
    this.newTJobImage = '';
    this.selectedSut = this.sutEmpty;
  }

  runTJob(tJob: TJobModel) {
    this.tJobService.runTJob(tJob.id)
      .subscribe(
      (tjobExecution) => {
        console.log('TJobExecutionId:' + tjobExecution.id);
        // this.createAndSubscribe(tjobExecution);
      },
      (error) => console.error('Error:' + error),
    );
  }
  editTJob(tJob: TJobModel) {
    this.router.navigate(['/tjobs-management/edit', tJob.id]);
  }
  deleteTJob(tJob: TJobModel) {
    let iConfirmConfig: IConfirmConfig = {
      message: 'TJob ' + tJob.id + ' will be deleted with all TJob Executions, do you want to continue?',
      disableClose: false,
      viewContainerRef: this._viewContainerRef,
      title: 'Confirm',
      cancelButton: 'Cancel',
      acceptButton: 'Yes, delete',
    };
    this._dialogService.openConfirm(iConfirmConfig).afterClosed().subscribe((accept: boolean) => {
      if (accept) {
        this.tJobService.deleteTJob(tJob).subscribe(
          (tJob) => this.reloadProjects(),
          (error) => console.log(error)
        );
      }
    });
  }


  // Suts functions
  createSut() {
    let sut: SutModel = new SutModel();
    sut.name = this.newSutName;
    sut.specification = this.newSutRepo;
    sut.description = this.newSutDesc;

    sut.project = this.projectSelected;

    this.sutService.createSut(sut).subscribe(
      (tJob) => {
        this.reloadProjects();
        this.clearSutForm();
      },
      (error) => console.log(error),
    );
  }

  clearSutForm() {
    this.newSutName = '';
    this.newSutRepo = '';
    this.newSutDesc = '';
  }

  editSut(sut: SutModel) {
    this.router.navigate(['/suts-management/edit', sut.id]);
  }
  deleteSut(sut: SutModel) {
    let iConfirmConfig: IConfirmConfig = {
      message: 'Sut ' + sut.id + ' will be deleted with all SuT Executions, do you want to continue? (SuT only will be deleted if hasn\'t associated TJobs)',
      disableClose: false,
      viewContainerRef: this._viewContainerRef,
      title: 'Confirm',
      cancelButton: 'Cancel',
      acceptButton: 'Yes, delete',
    };
    this._dialogService.openConfirm(iConfirmConfig).afterClosed().subscribe((accept: boolean) => {
      if (accept) {
        this.sutService.deleteSut(sut).subscribe(
          (sut) => this.reloadProjects(),
          (error) => console.log(error)
        );
      }
    });
  }


}
