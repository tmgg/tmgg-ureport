/**
 * Created by Jacky.Gao on 2017-02-05.
 */
import {alert} from '../MsgBox.js';
import {setDirty} from '../Utils.js';

export default class DatasourceDialog{
    constructor(datasources){
        this.datasources=datasources;
        this.dialog=$(`<div class="modal fade" role="dialog" aria-hidden="true" style="z-index: 10000">
            <div class="modal-dialog modal-lg">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">
                            &times;
                        </button>
                        <h4 class="modal-title">
                            数据源配置
                        </h4>
                    </div>
                    <div class="modal-body"></div>
                    <div class="modal-footer">
                    </div>
                </div>
            </div>
        </div>`);
        const body=this.dialog.find('.modal-body'),footer=this.dialog.find(".modal-footer");
        this.initBody(body,footer);
    }

    initBody(body,footer){
        const dsRow=$(`<div class="row" style="margin-bottom: 10px;margin-right:6px;"><div class="col-md-2" style="padding: 0 10px 0 0px;text-align:right;margin-top:5px">数据源名称：</div></div>`);
        const dsNameGroup=$(`<div class="col-md-10" style="padding: 0 10px 0 0px"></div>`);
        this.dsNameEditor=$(`<input type="text" class="form-control" style="font-size: 13px">`);
        dsNameGroup.append(this.dsNameEditor);
        dsRow.append(dsNameGroup);
        body.append(dsRow);

        const usernameRow=$(`<div class="row" style="margin-bottom: 10px;margin-right:6px;"><div class="col-md-2" style="padding: 0 10px 0 0px;text-align:right;margin-top:5px">连接用户名：</div></div>`);
        const usernameGroup=$(`<div class="col-md-10" style="padding: 0 10px 0 0px"></div>`);
        this.usernameEditor=$(`<input type="text" class="form-control" style="font-size: 13px">`);
        usernameGroup.append(this.usernameEditor);
        usernameRow.append(usernameGroup);
        body.append(usernameRow);

        const passwordRow=$(`<div class="row" style="margin-bottom: 10px;margin-right:6px;"><div class="col-md-2" style="padding: 0 10px 0 0px;text-align:right;margin-top:5px">连接密码：</div></div>`);
        const passwordGroup=$(`<div class="col-md-10" style="padding: 0 10px 0 0px"></div>`);
        this.passwordEditor=$(`<input type="password" class="form-control" style="font-size: 13px">`);
        passwordGroup.append(this.passwordEditor);
        passwordRow.append(passwordGroup);
        body.append(passwordRow);

        const driverRow=$(`<div class="row" style="margin-bottom: 10px;margin-right:6px;"><div class="col-md-2" style="padding: 0 10px 0 0px;text-align:right;margin-top:5px">驱动名称：</div></div>`);
        const driverGroup=$(`<div class="col-md-10" style="padding: 0 10px 0 0px"></div>`);
        this.driverEditor=$(`<input type="text" class="form-control" style="font-size: 13px">`);
        driverGroup.append(this.driverEditor);
        driverRow.append(driverGroup);
        body.append(driverRow);
        this.driverEditor.completer({
            source: [
                "oracle.jdbc.OracleDriver",
                "com.ibm.db2.jcc.DB2Driver",
                "com.microsoft.sqlserver.jdbc.SQLServerDriver",
                "com.mysql.jdbc.Driver"
            ],
            suggest: true,
            zIndex:200000
        });

        const urlRow=$(`<div class="row" style="margin-bottom: 10px;margin-right:6px;"><div class="col-md-2" style="padding: 0 10px 0 0px;text-align:right;margin-top:5px">连接URL：</div></div>`);
        const urlGroup=$(`<div class="col-md-10" style="padding: 0 10px 0 0px"></div>`);
        this.urlEditor=$(`<input type="text" class="form-control" style="font-size: 13px">`);
        urlGroup.append(this.urlEditor);
        urlRow.append(urlGroup);
        body.append(urlRow);
        this.urlEditor.completer({
            source: [
                "jdbc:oracle:thin:@localhost:1521:orcl",
                "jdbc:db2://localhost:50000/dbname",
                "jdbc:sqlserver://localhost:1433;databaseName=dbname",
                "jdbc:mysql://localhost:3306/dbname?useUnicode=true&characterEncoding=UTF-8"
            ],
            suggest: true,
            zIndex:200000
        });

        const _this=this;
        const testButton=$(`<button type="button" class="btn btn-default">测试连接</button>`);
        footer.append(testButton);
        testButton.click(function(){
            const dsName=_this.dsNameEditor.val(),username=_this.usernameEditor.val(),password=_this.passwordEditor.val(),driver=_this.driverEditor.val(),url=_this.urlEditor.val();
            _this.testConnection(dsName,username,password,driver,url);
        });
        const saveButton=$(`<button type="button" class="btn btn-primary">保存</button>`);
        footer.append(saveButton);
        saveButton.click(function(){
            const name=_this.dsNameEditor.val(),username=_this.usernameEditor.val(),password=_this.passwordEditor.val(),driver=_this.driverEditor.val(),url=_this.urlEditor.val();
            _this.testConnection(name,username,password,driver,url,function(){
                _this.onSave.call(this,name,username,password,driver,url);
                setDirty();
                _this.dialog.modal('hide');
            });
        });
    }

    testConnection(dsName,username,password,driver,url,callback){
        if(dsName===''){
            alert(`请输入数据源名称`);
            return;
        }
        if(username===''){
            alert(`请输入连接用户名`);
            return;
        }
        if(driver===''){
            alert(`请输入连接驱动`);
            return;
        }
        if(url===''){
            alert(`请输入连接URL`);
            return;
        }
        let check=false;
        if(!this.oldName || dsName!==this.oldName){
            check=true;
        }
        if(check){
            for(let source of this.datasources){
                if(source.name===dsName){
                    alert(`数据源[${dsName}]已存在，请更换当前数据源名称.`);
                    return;
                }
            }
        }
        const _this=this;
        $.ajax({
            url:window._server+"/datasource/testConnection",
            data:{username,password,driver,url},
            type:"POST",
            success:function(data){
                if(callback){
                    callback.call(_this);
                }else{
                    if(data.result){
                        alert(`连接测试成功！`);
                    }else{
                        alert(`连接测试失败：`+data.error);
                    }
                }
            },
            error:function(response){
                if(response && response.responseText){
                    alert("服务端错误："+response.responseText+"");
                }else{
                    alert(`连接测试失败，不能进行后续动作，请确认当前连接信息是否正确。`);
                }
            }
        });
    }

    show(onSave,ds){
        this.dialog.modal('show');
        this.onSave=onSave;
        if(ds){
            this.oldName=ds.name;
            this.dsNameEditor.val(ds.name);
            this.usernameEditor.val(ds.username);
            this.passwordEditor.val(ds.password);
            this.driverEditor.val(ds.driver);
            this.urlEditor.val(ds.url);
        }
    }
}

