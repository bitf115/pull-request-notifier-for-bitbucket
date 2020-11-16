import DynamicTable from '@atlaskit/dynamic-table';
import React, {useEffect, useState} from "react";
import TriggerButton from "./trigger-button";

interface INotificationProps{
    projectKey: string,
    repoSlug: string,
    repoID: number,
    prID: number
}

interface INotificationAction{
    uuid: string,
    name: string,
    userLevel: string, // change to enum
    redirectUrl: string
}

const caption = "Pull request notification"

const createHead = (withWidth: boolean) => {
    return {
        cells: [
            {
                key: 'notification',
                content: 'Notification',
                isSortable: false,
                width: withWidth ? 25 : undefined,
            },
            {
                key: 'action',
                content: 'Action',
                isSortable: false,
                width: withWidth ? 15 : undefined,
            },
        ],
    };
};

const head = createHead(true);

function createKey(input: string) {
    return input ? input.replace(/^(the|a|an)/, '').replace(/\s/g, '') : input;
}


function NotificationTable({projectKey,repoSlug, repoID, prID}: INotificationProps) {
    const [loading, setLoading] = useState(true);
    const [rows, setRows] = useState([{
        key: `0`,
        cells: [{key: createKey('loading'), content: `loading`}, {key: createKey('loading'), content: 'loading'}]
    }]);
    // @ts-ignore
    const contextPath = AJS.contextPath() ;

    useEffect(() => {
        getActions()
    }, [])

    function getActions(){
        fetch(
            contextPath + `/rest/prnfb-admin/1.0/settings/buttons/projectKey/${projectKey}/repositorySlug/${repoSlug}`,
        ).then(r => r.json()).then(result => {
            console.log(result);
            if (result.length) {
                setRows(result.map((action: INotificationAction, index: number) => ({
                    key: action.uuid,
                    cells: [{
                        key: "notification",
                        content: action.name
                    },{
                        key: "action",
                        content: (<TriggerButton uuid={action.uuid} repoID={repoID} prID={prID} notificationName={action.name}/>)
                    }
                    ]
                })))
                setLoading(false);
            }});
    }

    return <DynamicTable
        head={head}
        rows={rows}
        rowsPerPage={10}
        defaultPage={1}
        loadingSpinnerSize="large"
        isLoading={loading}
        isFixedSize
        defaultSortKey="notification"
        defaultSortOrder="ASC"
        onSort={() => console.log('onSort')}
        onSetPage={() => console.log('onSetPage')}
    />
}

export default NotificationTable