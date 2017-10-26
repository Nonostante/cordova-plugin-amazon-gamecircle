#!/usr/bin/env node

module.exports = function (ctx) {
    // make sure android platform is part of build
    if (ctx.opts.platforms.indexOf('android') < 0) {
        return;
    }
    var fs = ctx.requireCordovaModule('fs'),
        path = ctx.requireCordovaModule('path'),
        et = ctx.requireCordovaModule("elementtree"),
        deferral = ctx.requireCordovaModule('q').defer();

    var cordovaPath = path.join(ctx.opts.projectRoot, "config.xml");

    var cordovaConfig = new et.ElementTree(et.XML(fs.readFileSync(cordovaPath, "utf8").replace(/^\uFEFF/, "")));
    var widget = cordovaConfig.getroot();
    if (!widget) {
        console.error("Invalid cordova project config");
        return;
    }

    var node = widget.find('plugin/[@name="cordova-plugin-amazon-gamecircle"]/variable/[@name="API_KEY"]');
    var apiKey = node && node.attrib.value;
    if (!apiKey) {
        deferral.reject("Error! No API_KEY found in the GameCircle plugin config");
        return;
    }

    var filePath = path.join(ctx.opts.projectRoot, 'platforms/android/assets', 'api_key.txt');
    fs.writeFile(filePath, apiKey, "utf8", function (err) {
        if (err) {
            deferral.reject("Error writing GameCircle Key");
        } else {
            deferral.resolve();
        }
    });

    return deferral.promise;
};