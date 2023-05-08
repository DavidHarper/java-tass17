#!/usr/bin/env perl

use strict;

my $linenum = 0;

while (my $line = <STDIN>) {
    $linenum++;

    if ($line =~ /^\s+(\d+)\s+(\d+)\s*$/) {
        my ($satnum, $elnum) = ($1,$2);
        print "Line $linenum: $satnum $elnum";

        # Hyperion is a special case
        last if ($satnum == 7);

        my @data;
        my $ncrit = 0;

        while ($line = <STDIN>) {
            $linenum++;

            if ($line =~ /^\s*9999\s+/) {
                last;
            } elsif ($line =~ /^\s*9998\s+/) {
                $ncrit = scalar(@data);
            } else {
                $line =~ s/D/E/g;
                push @data,$line;
            }
        }

        my $nterms = scalar(@data);
        
        if ($elnum == 2) {
            $nterms--;
            $ncrit-- if ($ncrit > 0);
        }

        print " $ncrit $nterms\n";

        my $filename = sprintf("data/S%02d_%02d.dat", $satnum, $elnum);
        
        open ELEMFILE, "> $filename";
        print ELEMFILE "$satnum $elnum $nterms $ncrit\n";
        print ELEMFILE @data;
        close ELEMFILE;
    }
}

print "\n";

# Now we deal with Hyperion
my $satnum = 7;
my $elnum = 0;

while (my $line = <STDIN>) {
    $linenum++;

    if ($line =~ /^\s+(\d+)\s*$/) {
        my $nterms = $1;
        $elnum++;
        print "Line $linenum: $satnum $elnum $nterms\n";

        my $filename = sprintf("data/S%02d_%02d.dat", $satnum, $elnum);
        
        open ELEMFILE, "> $filename";
        print ELEMFILE "$satnum $elnum $nterms\n";

        # Elements 1 and 2 have an additional constant term which is not
        # included in the count of periodic terms.
        $nterms++ if ($elnum == 1 || $elnum == 2);

        for (my $i = 0; $i < $nterms; $i++) {
            $line = <STDIN>;
            $linenum++;
            $line =~ s/D/E/g;
            print ELEMFILE $line;
        }

        close ELEMFILE;
    }
}