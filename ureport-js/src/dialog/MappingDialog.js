/**
 * Created by Jacky.Gao on 2017-02-07.
 */
import {alert} from '../MsgBox.js';

export default class MappingDialog{
    constructor(){
        this.dialog=$(`<div class="modal fade" role="dialog" aria-hidden="true" style="z-index: 10000">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">
                            &times;
                        </button>
                        <h4 class="modal-title">
                            添加数据映射项
                        </h4>
                    </div>
                    <div class="modal-body"></div>
                    <div class="modal-footer"></div>
                </div>
            </div>
        </div>`);
        const body=this.dialog.find('.modal-body'),footer=this.dialog.find(".modal-footer");
        this.initBody(body,footer);
    }
    initBody(body,footer){
        const valueGroup=$(`<div class="form-group"><label>实际值：</label></div>`);
        body.append(valueGroup);
        this.valueEditor=$(`<input type="text" class="form-control" style="display: inline-block;width:500px;">`);
        valueGroup.append(this.valueEditor);
        const labelGroup=$(`<div class="form-group"><label>显示值：</label></div>`);
        this.labelEditor=$(`<input type="text" class="form-control" style="display: inline-block;width:500px;">`);
        labelGroup.append(this.labelEditor);
        body.append(labelGroup);

        const saveButton=$(`<button type="button" class="btn btn-primary">保存</button>`);
        footer.append(saveButton);
        const _this=this;
        saveButton.click(function(){
            const value=_this.valueEditor.val(),label=_this.labelEditor.val();
            if(value==='' || label===''){
                alert(`映射项请输入完整！`);
                return;
            }
            _this.mappingItem.value=value;
            _this.mappingItem.label=label;
            _this.callback.call(this);
            _this.dialog.modal('hide');
        });
    }
    show(callback,mappingItem,op){
        this.callback=callback;
        this.mappingItem=mappingItem;
        this.dialog.modal('show');
        if(op==='add'){
            this.dialog.find('.modal-title').html(`添加数据映射项`);
        }else{
            this.dialog.find('.modal-title').html(`编辑数据映射项`);
        }
        this.valueEditor.val(mappingItem.value);
        this.labelEditor.val(mappingItem.label);
    }
}