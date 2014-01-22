Arbiter.MediaSync = function(_dbLayers, _layerSchemas, _mediaDir, _mediaToSend){
	this.mediaToSend = _mediaToSend;
	this.mediaDir = _mediaDir;
	this.failedOnUpload = null;
	this.failedOnDownload = null;
	
	this.layers = _dbLayers;
	
	this.layerSchemas = _layerSchemas;
	this.queueDownloading = this.layers.length - 1;
	this.queuedUploading = this.layers.length - 1;
	
	this.index = -1;
	
	this.onSyncComplete = null;
	
	this.finishedUploading = -1;
	this.finishedDownloading = -1;
};

// url
// layerId
// auth headers

Arbiter.MediaSync.prototype.pop = function(){
	
	if(++this.index < this.layers.length){
		return this.layers[this.index];
	}
	
	return undefined;
};

Arbiter.MediaSync.prototype.startSync = function(onSuccess, onFailure, downloadOnly){
	var context = this;
	
	this.onSyncComplete = onSuccess;
		
	if(downloadOnly === true || downloadOnly === "true"){
		
		console.log("media sync download only");
		context.startDownloadForNext();
	}else{
		
		context.startUploadForNext();
	}
};

Arbiter.MediaSync.prototype.onUploadComplete = function(){
	// TODO: Handle errors
	console.log("MediaSync.js Upload completed.  The following"
			+ " failed to upload:", this.failedOnUpload);
	
	Arbiter.Cordova.finishMediaUploading();
	
	this.index = -1;
	
	this.startDownloadForNext();
};

Arbiter.MediaSync.prototype.onDownloadComplete = function(){
	// TODO: Handle errors
	console.log("MediaSync.js Download completed.  The following"
			+ " failed to download:", JSON.stringify(this.failedOnDownload));
	
	Arbiter.Cordova.finishMediaDownloading();
	
	if(Arbiter.Util.funcExists(this.onSyncComplete)){
		this.onSyncComplete(this.failedOnUpload,
				this.failedOnDownload);
	}
};

Arbiter.MediaSync.prototype.startUploadForNext = function(){
	var context = this;
	
	console.log("getting next to upload");
	
	var layer = this.pop();
	
	if(layer !== undefined && (this.mediaToSend !== null 
			&& this.mediaToSend !== undefined)){
		
		if(this.index === 0){
			Arbiter.Cordova.showMediaUploadingStatus(
					layer[Arbiter.LayersHelper.featureType()],
					context.queueUploading);
		}
		
		this.uploadMedia(layer);
	}else{
		this.onUploadComplete();
	}
};

Arbiter.MediaSync.prototype.startDownloadForNext = function(){
	var context = this;
	
	var layer = this.pop();
	
	if(layer !== undefined){
		
		if(this.index === 0){
			Arbiter.Cordova.showMediaDownloadingStatus(
					layer[Arbiter.LayersHelper.featureType()],
					context.queueDownloading);
		}
		
		this.downloadMedia(layer);
	}else{
		this.onDownloadComplete();
	}
};

Arbiter.MediaSync.prototype.putFailedUpload= function(key, failed){
	
	if(failed !== null && failed !== undefined){
		
		if(this.failedOnUpload === null 
				|| this.failedOnUpload === undefined){
			
			this.failedOnUpload = {};
		}
		
		this.failedOnUpload[key] = failed;
	}
};

Arbiter.MediaSync.prototype.uploadMedia = function(layer){
	var context = this;
	
	var layerId = layer[Arbiter.LayersHelper.layerId()];
	var serverId = layer[Arbiter.LayersHelper.serverId()];
	
	var mediaForLayer = this.mediaToSend[layerId];
	
	this.finishedUploading++;
	
	if(mediaForLayer === null 
			|| mediaForLayer === undefined 
			|| mediaForLayer.length === 0){
		
		this.startUploadForNext();
		
		return;
	}
	
	var server = Arbiter.Util.Servers.getServer(serverId);
	
	var mediaUploader = new Arbiter.MediaUploader(
			this.layerSchemas[layerId], mediaForLayer,
			server, context.mediaDir);
	
	mediaUploader.startUpload(function(failedMedia){
		
		context.putFailedUpload(layerId, failedMedia);
		
		context.startUploadForNext();
	});
};

Arbiter.MediaSync.prototype.putFailedDownload = function(key, failed){
	
	if(failed !== null && failed !== undefined){
		
		if(this.failedOnDownload === null 
				|| this.failedOnDownload === undefined){
			
			this.failedOnDownload = {};
		}
		
		this.failedOnDownload[key] = failed;
	}
};

Arbiter.MediaSync.prototype.downloadMedia = function(layer){
	var context = this;
	
	console.log("downloadMedia called");
	var layerId = layer[Arbiter.LayersHelper.layerId()];
	var serverId = layer[Arbiter.LayersHelper.serverId()];
	
	var schema = this.layerSchemas[layerId];
	
	var server = Arbiter.Util.Servers.getServer(serverId);
	
	var featureDb = Arbiter.FeatureDbHelper.getFeatureDatabase();
	
	this.finishedDownloading++;
	
	var mediaDownloader = new Arbiter.MediaDownloader(featureDb,
			schema, server, context.mediaDir);
	
	mediaDownloader.startDownload(function(failedMedia){
		
		context.putFailedDownload(layerId, failedMedia);
		
		context.startDownloadForNext();
	});
};