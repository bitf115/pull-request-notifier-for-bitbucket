import React from 'react';
import {ModalExtension} from '@atlassian/clientside-extensions';
import {Context, PluginAPI} from '@atlassian/clientside-extensions-registry/lib/types';
import {renderElementAsReact} from '@atlassian/clientside-extensions';
import NotificationTable from "./notification-table"


/**
 * @clientside-extension
 * @extension-point bitbucket.ui.pullrequest.overview.summary
 */
export default ModalExtension.factory((api: PluginAPI, context: Context<any>) => {
    const projectKey = context.project.key;
    const repoSlug = context.repository.slug;
    const repoID = context.repository.id;
    const prID = context.pullRequest.id;
    // @ts-ignore
    const contextPath = AJS.contextPath();

    function getLabel(buttonCount: number) {
        return `Pull request notification( found: ${buttonCount} )`
    }


    fetch(
        contextPath + `/rest/prnfb-admin/1.0/settings/buttons/projectKey/${projectKey}/repositorySlug/${repoSlug}`,
    ).then(r => r.json()).then(result => {
        console.log(result);
        if (result.length) {
            api.updateAttributes({
                label: getLabel(result.length),
                hidden: false
            })
        }
    });
    console.log(context);
    return {
        label: getLabel(0),
        onAction(modalApi) {
            modalApi.setTitle('Pull request notification').setWidth(ModalExtension.Width.large).setAppearance(ModalExtension.Appearance.warning);

            const ReactComponent = () => {
                modalApi.onClose(() => {
                    return window.confirm('Are you sure you want to close me?');
                });

                modalApi.setActions([
                    {
                        text: 'close',
                        onClick() {
                            modalApi.closeModal();
                        }
                    }
                ]);

                return (
                    <div data-testid="modal-with-action-callback">
                        <NotificationTable
                            projectKey={projectKey}
                            repoSlug={repoSlug}
                            repoID={repoID}
                            prID={prID}/>
                    </div>
                );
            };
            renderElementAsReact(modalApi, ReactComponent);
        },
    };
});