import Button from '@atlaskit/button';
import React, {useState} from "react";
import fetchOptions from "./fetch-options";
import {AutoDismissFlag, FlagGroup} from '@atlaskit/flag';
import SuccessIcon from '@atlaskit/icon/glyph/check-circle';
import { G400 } from '@atlaskit/theme/colors';


interface IButtonProps {
    /** uuid of button notification */
    uuid: string,
    /** repository ID number */
    repoID: number,
    /** pull request ID */
    prID: number,
    /** Notification name/description */
    notificationName: string
}


function TriggerButton({...props} :IButtonProps) {
    // @ts-ignore
    const contextPath = AJS.contextPath() ;
    const buttonsAdminUrl = contextPath + `/rest/prnfb-admin/1.0/settings/buttons/` + props.uuid +
        '/press/repository/' + props.repoID +
        '/pullrequest/' + props.prID;

    const [buttonDisabled, setButtonDisabled] = useState(false);
    const [flags, setFlags] = useState<Array<number>>([]);

    const addFlag = () => {
        const newFlagId = flags.length + 1;
        const newFlags = flags.slice();
        newFlags.splice(0, 0, newFlagId);

        setFlags(newFlags);
    };

    const handleDismiss = () => {
        setFlags(flags.slice(1));
        setButtonDisabled(false);
    };


    function sendNotification() {
        setButtonDisabled(true);
        fetch(buttonsAdminUrl, fetchOptions('POST')).then( response =>{
            if(!response.ok){
                throw new Error("error. response code:" + response.status)
            }
            addFlag()
        }).catch( e => {
            setButtonDisabled(false)
            }
        )
    }

    return <>
        <FlagGroup onDismissed={handleDismiss}>
            {flags.map(flagId => {
                return (
                    <AutoDismissFlag
                        appearance="success"
                        id={flagId}
                        icon={
                            <SuccessIcon
                                label="Success"
                                size="medium"
                                secondaryColor={G400}
                            />
                        }
                        key={flagId}
                        title={`Notification "${props.notificationName}" send`}
                        description="I will auto dismiss after 8 seconds"
                    />
                );
            })}
        </FlagGroup>
        <Button isDisabled={buttonDisabled} onClick={() => sendNotification()}>RUN</Button>
    </>
}
export default TriggerButton;