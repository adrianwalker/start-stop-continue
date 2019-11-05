#!/bin/sh

mkdir /var/tmp/ststpcnt.com
sudo chown ststpcnt:ststpcnt /var/tmp/ststpcnt.com

dd if=/dev/zero of=/var/tmp/ststpcnt.com.ext4 count=2097152
sudo chown ststpcnt:ststpcnt /var/tmp/ststpcnt.com.ext4

sudo mkfs -t ext4 -q /var/tmp/ststpcnt.com.ext4 -F
sudo mount -o loop /var/tmp/ststpcnt.com.ext4 /var/tmp/ststpcnt.com
sudo chown ststpcnt:ststpcnt /var/tmp/ststpcnt.com

grep 'ststpcnt.com' /proc/mounts
ls /var/tmp/ststpcnt.com

echo ""
echo "Add the following line to /etc/fstab"
echo "/var/tmp/ststpcnt.com.ext4    /var/tmp/ststpcnt.com ext4    rw,loop  0 0"
echo ""
