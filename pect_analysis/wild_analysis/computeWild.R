library(tidyverse)

#HELPER FUNCTIONS
#Returns a boolean to determine if the more than n blocks exists in the data set
#int n: the number of blocks to check
#string blockname: the name of the block to check
#dataframe dt: the dataframe to check
#Example: If blockname = "doifelse" and n = 1, it returns a TRUE if there exists more than 1 doifelse blocks in 
#the dataset
checkBlock = function(dt,blockname,n = 0){
  return(nrow(filter(dt, block==blockname)) != 0 && filter(dt, block==blockname)$num > n)
}

#Helper function used in compute dataset return a boolean to check if the characters in the block array #exists in the data set
#n: threshold of num of blocks to check
checkBlockArray = function(dt,blockArray,n = 0){
  for (i in 1:length(blockArray)){
    if (nrow(filter(dt, block==blockArray[i])) != 0 && filter(dt, block==blockArray[i])$num > n){
      return(TRUE)
    }
  }
  return(FALSE)
}

#dt = dtWildClean
#dt_key = dtKey, key
#respNum = number of project to get from
#PRECONDITION: column titles of scores in dtWildClean must be in the format "project#".
compute_dataset = function(dt, dt_key, respNum){
  resp = paste("project",respNum,sep="")
  #get scores of project #5
  #get dataset of project, the blocknames alongside how many exists in the project
  dt_resp = as.tibble(cbind(dt$block,dt[resp]))
  colnames(dt_resp) = c("V1","V2")
  dt_resp$V2 = as.numeric(dt_resp$V2) #set the numbers as numeric from character
  dt_resp_filter = dt_resp %>% #filter out blocks that do not exist
    filter(V2 != 0)
  colnames(dt_resp_filter) = (c("block","num")) 
  
  #map existing blocks with key table
  dt_resp_scores = left_join(dt_resp_filter,dt_key,by="block")
  dt_resp_scores = dt_resp_scores %>% #omit blocks that do not exist
    na.omit()
  
  #RESULT: dt_resp_scores = 
  #scores of projects, with category, block name, and the number of times it appears
  
  #initialize final data frame
  dt_return = as.tibble(data.frame(category = character(), ev_score = integer()))
  dt_return$category = as.character(dt_return$category)
  
  #compute scores of project based on category
  categoryVec = unique(dt_key$category)
  
  for (i in 1:length(categoryVec)){
    category_dt = filter(dt_resp_scores,category == categoryVec[i])
    if (!nrow(category_dt)==0) score_dt = c(categoryVec[i],max(category_dt$score))
    else score_dt = c(categoryVec[i],0)
    dt_return[i,] = score_dt
  }
  
  #assume for variables and sequences
  #for variables, always assume 1, as we assume a sprite is initialized in every project
  if (filter(dt_return,category=="variables")$ev_score == 0) { #if 0, change to 1
    dt_return[4,2] = 1
  }
  #for sequences, assume 1 if green flag block present
  if (filter(dt_return,category=="seq_looping")$ev_score == 0 & checkBlock(dt_resp_scores,"eventhatmorphstartclicked")) {
    dt_return[5,2] = 1
  }
  
  #compute parallelization, initialize location, and initialize looks
  #parallelization = 1 if 2 sprites start at same event, i.e. when two green flag blocks are present
  if(checkBlock(dt_resp_scores,"eventhatmorphstartclicked",1)) {
    dt_return[11,] = c("parallelization",1)
  } else {
    dt_return[11,] = c("parallelization",0)
  }
  
  #if more than one doifelse, assume nested ifelse 
  if (checkBlock(dt_resp_scores,"doifelse",1)){
    dt_return[8,2] = 3
  }
  
  #initialize location = 1 if green flag block is present(eventhatmorphstartclicked) and setxpos and setypos also exists
  if(checkBlock(dt_resp_scores,"eventhatmorphstartclicked") & (checkBlock(dt_resp_scores,"setxpos") |checkBlock(dt_resp_scores,"setypos"))) {
    dt_return[12,] = c("initialize_location",1)
  } else {
    dt_return[12,] = c("initialize_location",0)
  }
  
  lookBlocks = filter(filter(dt_key,category=="looks"), score != 1)
  #initialize looks = 1 if green flag block is present and looks exist (except say and think)
  if(checkBlock(dt_resp_scores,"eventhatmorphstartclicked") & (checkBlockArray(dt_resp_scores,lookBlocks$block)))   {
    dt_return[13,] = c("initialize_looks",1)
  } else {
    dt_return[13,] = c("initialize_looks",0)
  }
  dt_return
  return(dt_return)
}

#IMPORT KEY
dtkey = read_csv("ev_keys.csv")
dtkey = as.tibble(select(dtkey,block,score,category))

#IMPORT DATASET
dtWild = read_csv("project_input.csv")

#CLEAN DATASET
colnames(dtWild) = gsub("[[:punct:]]","",colnames(dtWild)) #clean column names
dtWildClean= as.tibble(cbind(colnames(dtWild),t(dtWild)))
dtWildClean = dtWildClean[-1,] #remove unecessary first row
#rename columns according to project ID/number
coln = vector()
for (i in 1:length(rownames(dtWild))){
  coln[i] = paste("project",i,sep="")
}
colnames(dtWildClean) = c("block",coln)

#COMPUTE
for (i in 2:length(colnames(dtWildClean))-1){
  if(i == 1){
    dt_final = compute_dataset(dtWildClean,dtkey,i)
    colnames(dt_final) = c("category",paste("project",i,sep=""))
  } else {
    dt_temp = compute_dataset(dtWildClean,dtkey,i)
    colnames(dt_temp) = c("category",paste("project",i,sep=""))
    dt_final = left_join(dt_final,dt_temp,by="category")
  }
}

#OUTPUT
dir.create(paste(getwd(),"/outputCSV",sep=""))
write.csv(dt_final,paste(getwd(),"/outputCSV/ev_output.csv",sep=""))

#VISUALIZE
df = read.csv(paste(getwd(),"/outputCSV/ev_output.csv",sep=""))

#function to prepare data for boxplot visualization
#catName = name of category to recreate data frame
reform = function(df, catName){
  cat = filter(df,category==catName)
  cat_t = as.data.frame(t(cat))
  cat_t = as.data.frame(cat_t[3:nrow(cat_t),])
  catrow = as.data.frame(rep(catName,nrow(cat_t)))
  dfinal = cbind(catrow,cat_t)
  colnames(dfinal) = c("category","score")
  dfinal$score = as.numeric(as.character(dfinal$score))
  return(dfinal)
}
cat_vector = as.character(df$category)
dfinal = data.frame()
for (i in cat_vector){
  dfinal = rbind(dfinal,reform(df,i))
}
reform(df,"sound")
plot = ggplot(data = dfinal) + geom_boxplot(mapping = aes(x = category, y = score, fill = "orange")) + theme(axis.text.x = element_text(angle = 30, hjust = 1)) + ggtitle("Wild Data Evidence Variables") + xlab("Category") + ylab("Score")+ guides(fill=FALSE)
dir.create(paste(getwd(),"/outputPlots",sep=""))
ggsave("wild_ev.png",plot,path=(paste(getwd(),"/outputPlots",sep="")))

#STATISTICS
dstat_input = read_csv(paste(getwd(),"/outputCSV/ev_output.csv",sep=""))
names = dstat_input$category
dstat_input = as.data.frame(t(dstat_input)[-1,])
dstat_input = dstat_input[-1,]
colnames(dstat_input) = names
factorToNumeric = function(vec){
  return(as.numeric(as.character(vec)))
}
#write statistics of results 
dstats = as.data.frame(
  rbind(c("looks",mean(factorToNumeric(dstat_input$looks)),sd(factorToNumeric(dstat_input$looks))),
        c("sound",mean(factorToNumeric(dstat_input$sound)),sd(factorToNumeric(dstat_input$sound))),
        c("motion",mean(factorToNumeric(dstat_input$motion)),sd(factorToNumeric(dstat_input$motion))),
        c("variables",mean(factorToNumeric(dstat_input$variables)),sd(factorToNumeric(dstat_input$variables))),
        c("seq_looping",mean(factorToNumeric(dstat_input$seq_looping)),sd(factorToNumeric(dstat_input$seq_looping))),
        c("boolean_exp",mean(factorToNumeric(dstat_input$boolean_exp)),sd(factorToNumeric(dstat_input$boolean_exp))),
        c("operators",mean(factorToNumeric(dstat_input$operators)),sd(factorToNumeric(dstat_input$operators))),
        c("conditional",mean(factorToNumeric(dstat_input$conditional)),sd(factorToNumeric(dstat_input$conditional))),
        c("coordination",mean(factorToNumeric(dstat_input$coordination)),sd(factorToNumeric(dstat_input$coordination))),
        c("ui_event",mean(factorToNumeric(dstat_input$ui_event)),sd(factorToNumeric(dstat_input$ui_event))),
        c("parallelization",mean(factorToNumeric(dstat_input$parallelization)),sd(factorToNumeric(dstat_input$parallelization))),
        c("initialize_location",mean(factorToNumeric(dstat_input$initialize_location)),sd(factorToNumeric(dstat_input$initialize_location))),
        c("initialize_looks",mean(factorToNumeric(dstat_input$initialize_looks)),sd(factorToNumeric(dstat_input$initialize_looks)))))
colnames(dstats) = c("evidence_variable","mean","standard_deviation")
write.csv(dstats,paste(getwd(),"/outputCSV/ev_statistics.csv",sep=""))


dplot = read_csv(paste(getwd(),"/outputCSV/ev_output.csv",sep=""))
ggplot(data = )


