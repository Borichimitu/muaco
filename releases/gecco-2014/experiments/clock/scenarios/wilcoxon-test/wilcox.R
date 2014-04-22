#Wilcoxon test
args <- commandArgs(trailingOnly = TRUE)

y1 = scan(file=args[1], what=double(0))
y2 = scan(file=args[2], what=double(0))

#t.test(y1, y2)
wilcox.test(y1, y2, paired=FALSE)
