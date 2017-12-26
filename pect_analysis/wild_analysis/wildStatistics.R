library(tidyverse)
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
