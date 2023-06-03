Convert text file with book in following format:
```
<part number>
line1
line2
```
avery part must ended with one of variant:
-  next: part number
- variant: variant text-part number
- the end

example:
```
1
sdf
sdf
next: 2
2
sdf
sdf
variant: true-3
variant: false-4
3
sdf
sdf
the end
4
sdf
sdf
the end
```