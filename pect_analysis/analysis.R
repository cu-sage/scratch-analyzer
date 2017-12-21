library(tidyverse)

#HELPER FUNCTIONS
#Returns a boolean to determine if the more than n blocks exists in the data set
#int n: the number of blocks to check
#string blockname: the name of the block to check
#dataframe dt: the dataframe to check
#Example: If blockname = "doifelse" and n = 1, it returns a TRUE if there exists more than 1 doifelse blocks in 
#the dataset
checkBlock = function(dt,blockname,n = 0){
  return(nrow(filter(dt, block==blockname)) != 0 && filter(dt, block==blockname)$count > n)
}

#Helper function used in compute dataset return a boolean to check if the characters in the block array #exists in the data set
#n: threshold of num of blocks to check
checkBlockArray = function(dt,blockArray,n = 0){
  for (i in 1:length(blockArray)){
    if (nrow(filter(dt, block==blockArray[i])) != 0 && filter(dt, block==blockArray[i])$count > n){
      return(TRUE)
    }
  }
  return(FALSE)
}

#Helper function to compute evidence variables for each project. 
#Returns a data frame containing category with its scores, as well as userID and projectID 
#format: userID / projectID / operators score / variables score / other categories...
computeProject = function(dfkey, project, userIDnum, projectIDnum){
  #initialize final data frame
  dt_return = as.tibble(data.frame(category = character(), ev_score = integer()))
  dt_return$category = as.character(dt_return$category)
  categoryVec = unique(dfkey$category) #list of categories (operators, etc.)
  #compute for each category 
  for (i in 1:length(categoryVec)){
    category_dt = filter(project,category == categoryVec[i]) 
    #if category exists in project, get maximum score
    if (!nrow(category_dt)==0) score_dt = c(categoryVec[i],max(category_dt$score)) 
    else score_dt = c(categoryVec[i],0) #else set it to zero
    dt_return[i,] = score_dt #put in final data frame
  }
  #assume for variables and sequences
  #for variables, always assume 1, as we assume a sprite is initialized in every project
  if (filter(dt_return,category=="variables")$ev_score == 0) { #if 0, change to 1
    dt_return[dt_return$category=="variables",2] = 1
  }
  #for sequences, assume 1 if green flag block present
  if (filter(dt_return,category=="seq_looping")$ev_score == 0 & checkBlock(project,"whengreenflag")) {
    dt_return[dt_return$category=="seq_looping",2] = 1
  }
  
  #compute parallelization, initialize location, and initialize looks
  #parallelization = 1 if 2 sprites start at same event, i.e. when two green flag blocks are present
  if(checkBlock(project,"whengreenflag",1)) {
    dt_return[11,] = c("parallelization",1)
  } else {
    dt_return[11,] = c("parallelization",0)
  }
  
  #if more than one doifelse, assume nested ifelse 
  if (checkBlockArray(project,c("doifelse","doif","doforeverif"),1)){
    dt_return[dt_return$category=="conditional",2] = 3
  }
  
  #initialize location = 1 if green flag block is present(eventhatmorphstartclicked) and setxpos and setypos also exists
  if(checkBlock(project,"whengreenflag") & (checkBlock(project,"xpos") |checkBlock(project,"ypos"))) {
    dt_return[12,] = c("initialize_location",1)
  } else {
    dt_return[12,] = c("initialize_location",0)
  }
  
  lookBlocks = filter(filter(dfkey,category=="looks"), score != 1)
  #initialize looks = 1 if green flag block is present and looks exist (except say and think)
  if(checkBlock(project,"whengreenflag") & (checkBlockArray(project,lookBlocks$block)))   {
    dt_return[13,] = c("initialize_looks",1)
  } else {
    dt_return[13,] = c("initialize_looks",0)
  }
  dt_return_final = data.frame(rbind(dt_return$ev_score))
  colnames(dt_return_final) = dt_return$category
  dt_return_final = cbind(userID = c(userIDnum), projectID = c(projectIDnum),dt_return_final)
  return(dt_return_final)
}

#NEED TO NOT BE PLATFORM BASED
df = read_csv("/Users/TimGimi/sageMaster/scratch-analyzer/example_input_output/Output/RegularOutput/dispatched/dispatch_perProject.csv")
colnames(df) = c("userID","projectID","block","count")
df = filter(df,!is.na(block))
dfkey = read_csv(paste(getwd(),"/ev_keys.csv",sep=""))[,-1]
df$block = tolower(gsub(":","",df$block))
df$block = gsub("_","",df$block)
#initialize list to bind
datalist = list()
k = 1;
#loop through all users
for(i in 1:max(df$userID)){
  user = filter(df,userID==i)
  #loop through all projects
  for(j in 1:max(user$projectID)){
    project = filter(user,projectID==j)
    project = inner_join(project,dfkey,by="block")
    datalist[[k]] = computeProject(dfkey,project,i,j)
    k = k+1 #counter for list
  }
}
#finally bind everything
dfinal = do.call(rbind,datalist)
#prepare test csv
dtest = filter(dfinal, userID == 6)
testName = as.tibble(c("all","basic","developing","inLocation","inLooks","none","parallelmany","parallelOne","proficient"))
colnames(testName) = c("testName")
dtest = cbind(testName,dtest)

#write CSV of all results
dir.create(paste(getwd(),"/outputCSV",sep=""))
write.csv(dfinal,paste(getwd(),"/outputCSV/ev_results.csv",sep=""))
write.csv(dtest,paste(getwd(),"/outputCSV/ev_tests.csv",sep="")) #tests

#write statistics of results 
dstat_input =read_csv(paste(getwd(),"/outputCSV/ev_results.csv",sep=""))
dstats = as.data.frame(rbind(c("looks",mean(dstat_input$looks),sd(dstat_input$looks)),
                             c("sound",mean(dstat_input$sound),sd(dstat_input$sound)),
                             c("motion",mean(dstat_input$motion),sd(dstat_input$motion)),
                             c("variables",mean(dstat_input$variables),sd(dstat_input$variables)),
                             c("seq_looping",mean(dstat_input$seq_looping),sd(dstat_input$seq_looping)),
                             c("boolean_exp",mean(dstat_input$boolean_exp),sd(dstat_input$boolean_exp)),
                             c("operators",mean(dstat_input$operators),sd(dstat_input$operators)),
                             c("conditional",mean(dstat_input$conditional),sd(dstat_input$conditional)),
                             c("coordination",mean(dstat_input$coordination),sd(dstat_input$coordination)),
                             c("ui_event",mean(dstat_input$ui_event),sd(dstat_input$ui_event)),
                             c("parallelization",mean(dstat_input$parallelization),sd(dstat_input$parallelization)),
                             c("initialize_location",mean(dstat_input$initialize_location),sd(dstat_input$initialize_location)),
                             c("initialize_looks",mean(dstat_input$initialize_looks),sd(dstat_input$initialize_looks))))
colnames(dstats) = c("evidence_variable","mean","standard_deviation")
write.csv(dstats,paste(getwd(),"/outputCSV/ev_statistics.csv",sep=""))

#VISUALIZATION
#PLOT OF EACH PROJECT
dir.create(paste(getwd(),"/outputPlots",sep=""))
ev_name = colnames(dfinal)
for (i in 1:max(dfinal$userID)){
  user = filter(dfinal,userID == i)
  for (j in 1:max(user$projectID)) {
    project_t = slice(as.data.frame(cbind(ev_name,t(filter(user,projectID==j)))),3:15)
    colnames(project_t) = c("ev_name","scores")
    plot = ggplot(data = project_t, aes(x = ev_name, y = scores)) + geom_bar(stat = "identity",colour = "black", fill = "orange", width = 0.7) + xlab("Evidence Variables") + ylab("Scores") + ggtitle(paste("Project ",j," Evidence Variable Scores",sep ='')) + guides(fill=FALSE) + theme(axis.text.x = element_text(angle = 30, hjust = 1))
    dir.create(paste(getwd(),"/outputPlots/users",i,sep=""))
    ggsave(paste("project",j,".png",sep=""),plot,path=(paste(getwd(),"/outputPlots/users",i,sep="")))
  }
}

#OUTPUT BLOCKS THAT ARE IN NEWER VERSION OF SCRATCH
blockListWild = read_csv("blockListWild.csv")
blockListSAGE = read_csv("blockListSAGE.csv")
blockListWild = blockListWild$Variable
blockListWild = as.tibble(tolower(gsub("_","",blockListWild)))
blockListSAGE = as.tibble(tolower(gsub(":","",blockListSAGE$block)))
colnames(blockListWild) = "block"
colnames(blockListSAGE) = "block"
blockListWild[,2] = rep(1,172)
blockListSAGE[,2] = rep(1,187)
lengthSAGE = length(blockListSAGE$block)
lengthWild = length(blockListWild$block)
notInclude = left_join(blockListSAGE,blockListWild,by="block")
notInclude = notInclude[is.na(notInclude$V2.y),]
write.csv(notInclude,paste(getwd(),"/outputCSV/notInclude.csv",sep=""))