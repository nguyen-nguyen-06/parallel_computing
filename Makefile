JAVAC = javac
JAVA  = java
OUT   = out

SORT_SRC = parallel_sorting/Sorting.java \
           parallel_sorting/Sequential_quick.java \
           parallel_sorting/Sequential_merge.java \
           parallel_sorting/Parallel_quick.java \
           parallel_sorting/Parallel_quick_simple.java \
           parallel_sorting/Parallel_merge.java \
           parallel_sorting/BenchMark.java

MATRIX_SRC = parallel_matrix_mul/Matrix_mul.java \
             parallel_matrix_mul/Sequential_matrix.java \
             parallel_matrix_mul/Parallel_matrix.java \
             parallel_matrix_mul/BenchMark.java

.PHONY: all compile run benchmark-sorting benchmark-matrix benchmark clean

all: compile

$(OUT):
	mkdir -p $(OUT)

compile: $(OUT)
	$(JAVAC) -d $(OUT) $(SORT_SRC)
	$(JAVAC) -d $(OUT) $(MATRIX_SRC)
	$(JAVAC) -d $(OUT) -cp $(OUT) Client.java

run: compile
	$(JAVA) -cp $(OUT) parallel_computing.Client

benchmark-sorting: compile
	$(JAVA) -cp $(OUT) parallel_computing.parallel_sorting.BenchMark

benchmark-matrix: compile
	$(JAVA) -cp $(OUT) parallel_computing.parallel_matrix_mul.BenchMark

benchmark: compile
	$(JAVA) -cp $(OUT) parallel_computing.parallel_sorting.BenchMark
	$(JAVA) -cp $(OUT) parallel_computing.parallel_matrix_mul.BenchMark

clean:
	rm -rf $(OUT) *.csv sorting_output.txt matrix_output.txt

