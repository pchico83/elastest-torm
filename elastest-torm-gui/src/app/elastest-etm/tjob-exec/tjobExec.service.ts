import { PopupService } from '../../shared/services/popup.service';
import { ETModelsTransformServices } from '../../shared/services/et-models-transform.service';
import { EsmServiceModel } from '../../elastest-esm/esm-service.model';
import { DashboardConfigModel } from '../tjob/dashboard-config-model';
import { ConfigurationService } from '../../config/configuration-service.service';
import { SutExecModel } from '../sut-exec/sutExec-model';
import { SutModel } from '../sut/sut-model';
import { TJobModel } from '../tjob/tjob-model';
import { TJobExecModel } from './tjobExec-model';
import { Http, Response } from '@angular/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Rx';
import 'rxjs/Rx';

@Injectable()
export class TJobExecService {
  constructor(
    private http: Http,
    private configurationService: ConfigurationService,
    private eTModelsTransformServices: ETModelsTransformServices,
    public popupService: PopupService,
  ) {}

  //  TJobExecution functions
  public runTJob(tJobId: number, parameters?: any[], sutParams?: any[]): Observable<TJobExecModel> {
    let url: string = this.configurationService.configModel.hostApi + '/tjob/' + tJobId + '/exec';
    let body: any = {};
    if (parameters) {
      body['tJobParams'] = parameters;
    }

    if (sutParams) {
      body['sutParams'] = sutParams;
    }

    return this.http.post(url, body).map((response: Response) => {
      let data: any = response.json();
      if (response.status !== 200) {
        let msg: string = 'Sut instrumented by Elastest is still activating beats. Wait and try again';
        this.popupService.openSnackBar(response.status + ' Code: ' + msg, 'OK', 5000);
        throw new Error(msg);
      } else {
        if (data !== undefined && data !== null) {
          return this.eTModelsTransformServices.jsonToTJobExecModel(response.json());
        } else {
          throw new Error('Empty response.');
        }
      }
    });
  }

  // Get all Executions of a TJob
  public getTJobsExecutions(tJob: TJobModel): Observable<TJobExecModel[]> {
    let url: string = this.configurationService.configModel.hostApi + '/tjob/' + tJob.id + '/exec';
    return this.http.get(url).map((response) => this.eTModelsTransformServices.jsonToTJobExecsList(response.json()));
  }

  public getTJobExecutionFiles(tJobId: number, tJobExecId: number) {
    let url: string = this.configurationService.configModel.hostApi + '/tjob/' + tJobId + '/exec/' + tJobExecId + '/files';
    return this.http.get(url).map((response) => response.json());
  }

  /*public getTJobExecutionFiles(tJobExec: TJobExecModel){
    let url: string = this.configurationService.configModel.hostApi + '/tjob/' + tJobExec.tJob.id + '/exec/' + tJobExec.id + '/files';
    return this.http.get(url)
    .map((response) => console.log(response.json()));
  }*/

  // Get a specific TJob Execution
  public getTJobExecution(tJob: TJobModel, idTJobExecution: number): Observable<TJobExecModel> {
    return this.getTJobExecutionByTJobId(tJob.id, idTJobExecution);
  }

  public getAllTJobExecs(): Observable<TJobExecModel[]> {
    let url: string = this.configurationService.configModel.hostApi + '/tjob/execs';
    return this.http.get(url).map((response) => {
      let data: any = response.json();
      if (data !== undefined && data !== null) {
        return this.eTModelsTransformServices.jsonToTJobExecsList(data);
      } else {
        throw new Error("Empty response. There are not TJobExecutions or you don't have permissions to access them");
      }
    });
  }
  public getTJobExecutionByTJobId(tJobId: number, idTJobExecution: number): Observable<TJobExecModel> {
    let url: string = this.configurationService.configModel.hostApi + '/tjob/' + tJobId + '/exec/' + idTJobExecution;
    return this.http.get(url).map((response) => {
      let data: any = response.json();
      if (data !== undefined && data !== null) {
        return this.eTModelsTransformServices.jsonToTJobExecModel(data);
      } else {
        throw new Error("Empty response. TJob Execution not exist or you don't have permissions to access it");
      }
    });
  }

  public stopTJobExecution(tJob: TJobModel, tJobExecution: TJobExecModel): Observable<TJobExecModel> {
    let url: string = this.configurationService.configModel.hostApi + '/tjob/' + tJob.id + '/exec/' + tJobExecution.id + '/stop';
    return this.http.delete(url).map((response) => {
      let data: any = response.json();
      if (data !== undefined && data !== null) {
        return this.eTModelsTransformServices.jsonToTJobExecModel(data);
      } else {
        throw new Error("Empty response. TJob Execution not exist or you don't have permissions to access it");
      }
    });
  }

  public deleteTJobExecution(tJob: TJobModel, tJobExecution: TJobExecModel): Observable<any> {
    let url: string = this.configurationService.configModel.hostApi + '/tjob/' + tJob.id + '/exec/' + tJobExecution.id;
    return this.http.delete(url).map((response) => response.json());
  }

  public getResultStatus(tJob: TJobModel, tJobExecution: TJobExecModel): Observable<any> {
    let url: string =
      this.configurationService.configModel.hostApi + '/tjob/' + tJob.id + '/exec/' + tJobExecution.id + '/result';
    return this.http.get(url).map((response) => response.json());
  }
}
