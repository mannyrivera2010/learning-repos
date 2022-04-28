#!/bin/bash

# Volume list file will have volume-id:Volume-name format

VOLUMES_LIST="/home/bitnami/snapshotFiles/snapshotVolumes.txt"
/usr/local/bin/aws ec2 describe-volumes --query 'Volumes[*].[VolumeId,Tags[].Value]' --output text | awk 'NR%2{printf "%s ",$0;next;}1'  > ~/snapshotFiles/snapshotVolumes.txt
SNAPSHOT_INFO_FILE="/home/bitnami/snapshotFiles/snapshotInfo.txt"
DATE=`date +%Y-%m-%d`
REGION="us-east-1"

# Snapshots Retention Period for each volume snapshot
RETENTION=7

SNAP_CREATION=/var/log/snap_creation
SNAP_DELETION=/var/log/snap_deletion

EMAIL_LIST=austimus5@gmail.com

echo "List of Snapshots Creation Status on `date`" >> $SNAP_CREATION
echo "List of Snapshots Deletion Status on `date`" >> $SNAP_DELETION

# Check whether the volumes list file is available or not?
if [ -f $VOLUMES_LIST ]; then

# Creating Snapshot for each volume using for loop
    while IFS=' ' read -r line || [[ -n "$line" ]]; do
        VOL_ID=`echo $line | awk -F" " '{print $1}'`
        VOL_NAME=`echo $line | awk -F" " '{print $2}'`
        # Creating the Snapshot of the Volumes with Proper Description.

        DESCRIPTION="${VOL_NAME}_${DATE}"
        #/usr/local/bin/aws ec2 create-snapshot --volume-id $VOL_ID --description "$DESCRIPTION" --region $REGION &>> $SNAP_CREATION
        SNAP_ID=`/usr/local/bin/aws ec2 create-snapshot --volume-id $VOL_ID --description $DESCRIPTION --region $REGION --query 'SnapshotId' --output text`
        echo "Snapshot Created for $VOL_NAME - $SNAP_ID - `date`" >> $SNAP_CREATION
        aws ec2 create-tags --resources $SNAP_ID --tags Key="Name",Value=$VOL_NAME

        /usr/local/bin/aws ec2 describe-snapshots --query Snapshots[*].[SnapshotId,VolumeId,Description,StartTime] --output text --filters "Name=status,Values=completed" "Name=volume-id,Values=$VOL_ID" | grep -v "CreateImage" > $SNAPSHOT_INFO_FILE
        while read SNAP_INFO; do
            ########echo "reading snap info: $SNAP_INFO"
            SNAP_ID=`echo $SNAP_INFO | awk '{print $1}'`
            SNAP_DATE=`echo $SNAP_INFO | awk '{print $4}' | awk -F"T" '{print $1}'`
            ########echo "Checking $SNAP_ID made on $SNAP_DATE"
            RETENTION_DIFF=`echo $(($(($(date -d "$DATE" "+%s") - $(date -d "$SNAP_DATE" "+%s"))) / 86400))`
            ########echo "Snapshot was made $RETENTION_DIFF days ago."
            if [ $RETENTION -lt $RETENTION_DIFF ]; then
                /usr/local/bin/aws ec2 delete-snapshot --snapshot-id $SNAP_ID --region $REGION --output text >> $SNAP_DELETION
                echo "DELETING $VOL_NAME -  $SNAP_INFO `date`">> $SNAP_DELETION
            fi
        done < "$SNAPSHOT_INFO_FILE"


    done < "$VOLUMES_LIST"
else
    echo "Volumes list file is not available : $VOLUMES_LIST Exiting." | echo "Snapshot Creation Status"
    exit 1
fi
