KernelUImage='linux-3.4.47/arch/arm/boot/uImage'
BootImage='u-boot/MLO'
 
UpdateBoxDevel()
{
	#Preparing your Development Machine
	echo deb http://www.emdebian.org/debian/ wheezy main >> /etc/apt/sources.list
	apt-get -y install emdebian-archive-keyring
	apt-get update
	apt-get -y install libc6-armel-cross libc6-dev-armel-cross
	apt-get -y install libc6-armhf-cross libc6-dev-armhf-cross
	apt-get -y install binutils-arm-linux-gnueabi
	apt-get -y install gcc-arm-linux-gnueabi 
	apt-get -y install gcc-4.7-arm-linux-gnueabi
	apt-get -y install g++-4.7-arm-linux-gnueabi
	apt-get -y install uboot-mkimage
	apt-get -y install libncurses5-dev
	apt-get -y install git bc
	apt-get -y install debootstrap dpkg-dev
	apt-get -y install qemu binfmt-support qemu-user-static dpkg-cross qemu-system
	apt-get -y install build-essential ruby-dev rubygems
	gem install fpm
}
 
DownloadBuildLinuxKernel()
{
	#Download Kernel + Config file
	wget https://www.kernel.org/pub/linux/kernel/v3.x/linux-3.4.47.tar.xz
	tar xvf linux-3.4.47.tar.xz
	git clone git://www.sakoman.com/git/meta-sakoman
	cp meta-sakoman/recipes-kernel/linux/linux-sakoman-3.2/omap3-multi/defconfig linux-3.4.47/arch/arm/configs/omap3_defconfig
	(cd linux-3.4.47 && make ARCH=arm CROSS_COMPILE=arm-linux-gnueabi- omap3_defconfig)
	(cd linux-3.4.47 && make ARCH=arm CROSS_COMPILE=arm-linux-gnueabi- uImage -j4)
	(cd linux-3.4.47 && make ARCH=arm CROSS_COMPILE=arm-linux-gnueabi- modules -j4)
}
 
CopyBootAndKernelImagesToRootFS()
{
	cp u-boot/MLO boot/
	cp u-boot/u-boot.bin boot/
	cp u-boot/u-boot.img boot/
	cp linux-3.4.47/arch/arm/boot/uImage boot/
}
 
MakeBootImages()
{
	###Making boot Images
	mkdir boot
	git clone git://github.com/gumstix/u-boot.git
	(cd u-boot && git checkout omap-v2012.10)
	(cd u-boot && make ARCH=arm CROSS_COMPILE=arm-linux-gnueabi- omap3_overo_config)
	(cd u-boot && make ARCH=arm CROSS_COMPILE=arm-linux-gnueabi- all)
	CopyBootAndKernelImagesToRootFS  ###FUNCTION CALL
}
 
CopyKernelToRootFS()
{
	###Install kernel to rootfs
	(cd linux-3.4.47 && make ARCH=arm modules_install INSTALL_MOD_PATH=../rootfs-temp)
	cp -avfr rootfs-temp/lib/modules/ armhf-rootfs/lib/
	cp -avfr rootfs-temp/lib/firmware/ armhf-rootfs/lib/
	rm -rf rootfs-temp
}
 
#CopyKernelToRootFS()
#{
#	###Install kernel to rootfs
#	(cd linux-3.4.47 && make ARCH=arm modules_install INSTALL_MOD_PATH=../armhf-rootfs)
#	rm armhf-rootfs/lib/modules/3.4.47/build
#	rm armhf-rootfs/lib/modules/3.4.47/source
#}
 
 
MountSpecial()
{
	echo 'Mounting Special Drives'
	(cd armhf-rootfs && mount -o bind /proc proc)
	(cd armhf-rootfs && mount -o bind /dev dev)
	(cd armhf-rootfs && mount -o bind /sys sys)
	(cd armhf-rootfs && mount -t devpts devpts dev/pts)
}
 
UmountSpecial()
{
	echo 'unmounting Special Drives'
	(cd armhf-rootfs && umount dev/pts)
	(cd armhf-rootfs && umount sys)
	(cd armhf-rootfs && umount dev)
	(cd armhf-rootfs && umount proc)
}
 
MakeRootFS()
{
	###Making a Rootfs for debian armel
	debootstrap --foreign --arch armhf wheezy armhf-rootfs
	(cd armhf-rootfs && cp /usr/bin/qemu-arm-static usr/bin)
 
	MountSpecial ###FUNCTION CALL
 
	(cd armhf-rootfs && LC_ALL=C LANGUAGE=C LANG=C chroot . /debootstrap/debootstrap --second-stage)
	(cd armhf-rootfs && LC_ALL=C LANGUAGE=C LANG=C chroot . dpkg --configure -a)
 
	CopyKernelToRootFS ###FUNCTION CALL
 
	(cd armhf-rootfs && echo bts-gumstix-B01 > etc/hostname)
	(cd armhf-rootfs && echo nameserver 8.8.8.8 > etc/resolv.conf)
	(cd armhf-rootfs && echo nameserver 8.8.4.4 >> etc/resolv.conf)
	(cd armhf-rootfs && echo deb http://ftp.debian.org/debian stable main contrib non-free > etc/apt/sources.list)
	(cd armhf-rootfs && echo deb-src http://ftp.debian.org/debian stable main contrib non-free >> etc/apt/sources.list)
	(cd armhf-rootfs && echo deb http://ftp.debian.org/debian/ wheezy-updates main contrib non-free  >> etc/apt/sources.list)
	(cd armhf-rootfs && echo deb-src http://ftp.debian.org/debian/ wheezy-updates main contrib non-free  >> etc/apt/sources.list)
	(cd armhf-rootfs && echo deb http://security.debian.org/ wheezy/updates main contrib non-free >> etc/apt/sources.list)
	(cd armhf-rootfs && echo deb-src http://security.debian.org/ wheezy/updates main contrib non-free >> etc/apt/sources.list)
	(cd armhf-rootfs && echo auto eth0 >> etc/network/interfaces)
	(cd armhf-rootfs && echo iface eth0 inet dhcp >> etc/network/interfaces)
 
	##Configuring RootFS
	echo 'Type in the Password for the root user in the roots:'
	(cd armhf-rootfs &&  chroot . passwd)
	#Installing Packages
	(cd armhf-rootfs && chroot . apt-get update)
	(cd armhf-rootfs && LC_ALL=C LANGUAGE=C LANG=C chroot . apt-get upgrade)
	(cd armhf-rootfs && LC_ALL=C LANGUAGE=C LANG=C chroot . apt-get -y install openssh-server)
	(cd armhf-rootfs && LC_ALL=C LANGUAGE=C LANG=C chroot . /etc/init.d/ssh stop)
	(cd armhf-rootfs && LC_ALL=C LANGUAGE=C LANG=C chroot . apt-get -y install dialog locales dpkg-dev)
	echo 'Select en_us.UTF8'
	(cd armhf-rootfs && LC_ALL=C LANGUAGE=C LANG=C chroot . dpkg-reconfigure locales)
	(cd armhf-rootfs && echo \#Mount the fat16 partition where is located > etc/fstab)
	(cd armhf-rootfs && echo \#the Kernel image >> etc/fstab)
	(cd armhf-rootfs && echo \/dev/mmcblk0p1 /boot vfat noatime 0 1 >> etc/fstab)
	(cd armhf-rootfs && echo \#Mount the rootfs partition >> etc/fstab)
	(cd armhf-rootfs && echo \/dev/mmcblk0p2 / ext3 noatime 0 1 >> etc/fstab)
	(cd armhf-rootfs && echo proc /proc procpa defaults 0 0 >> etc/fstab)
	#Installing Misc Pages
	(cd armhf-rootfs && LC_ALL=C LANGUAGE=C LANG=C chroot . apt-get -y install gpsd gpsd-clients)
	(cd armhf-rootfs && LC_ALL=C LANGUAGE=C LANG=C chroot . apt-get -y install sudo)
	echo 'Adding BTS USER'
	(cd armhf-rootfs && LC_ALL=C LANGUAGE=C LANG=C chroot . adduser bts)
	(cd armhf-rootfs && LC_ALL=C LANGUAGE=C LANG=C chroot . adduser bts sudo)
	###Installing NodeJS
	echo 'Downloading installing NODEJS'
	wget https://gist.github.com/adammw/3245130/raw/v0.10.10/node-v0.10.10-linux-arm-armv6j-vfp-hard.tar.gz
	tar xvf node-v0.10.10-linux-arm-armv6j-vfp-hard.tar.gz -C armhf-rootfs/opt
	mv armhf-rootfs/opt/node-v0.10.10-linux-arm-armv6j-vfp-hard/ armhf-rootfs/opt/node
	echo NODE_JS_HOME="/opt/node" > armhf-rootfs/etc/profile.d/nodejs
	echo 'PATH="$PATH:$NODE_JS_HOME/bin"' >> armhf-rootfs/etc/profile.d/nodejs
	echo export PATH  >> armhf-rootfs/etc/profile.d/nodejs
	chmod +x armhf-rootfs/etc/profile.d/nodejs 
 
 
	UmountSpecial ###FUNCTION CALL
	#CleanUP
	rm armhf-rootfs/usr/bin/qemu-arm-static 
 
}
 
MakeCompressedBackupOfRootFS()
{
	rm armhf-rootfs.tar.bz2
	(cd armhf-rootfs/ && tar cpjvf ../armhf-rootfs.tar.bz2 .)
}
 
UpdateBoxDevel  ###FUNCTION CALL
 
if [ -f $KernelUImage ];
then
   echo "File $FILE exists ... Skip Compiling"
else
   echo "File $FILE does not exists"
   DownloadBuildLinuxKernel  ###FUNCTION CALL
fi
 
if [ -f $BootImage ];
then
   echo "File $FILE exists ... Skip Compiling"
else
   echo "File $FILE does not exists"
   MakeBootImages  ###FUNCTION CALL
fi
 
MakeRootFS ###FUNCTION CALL
 
 
###MakeCompressedBackupOfRootFS ###FUNCTION CALL
echo 'END OF INSTALLATION'