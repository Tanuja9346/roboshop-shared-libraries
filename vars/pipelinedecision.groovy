#!groovy
//inngroovy defining variable and function are same.
def decidepipeline(Map configMap){
    application = configMap.get("application")
    //here we are getting nodejsvm
    switch(application) {
        case 'nodeJSVM':
         echo "application is Nodejs and vm based"
            nodeJSVMCI(configMap)    //here directly calling 
            break
        case 'javavm':
              javaVMCI(configMap)
              break
        default: 
          error: "unrecognized application"
          break
    }
//echo "i need to take decision based on the map you sent "---1st practice line
}