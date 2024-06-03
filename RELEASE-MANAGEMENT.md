# Release Management of UN/LOCODE for JSON-LD publication

This repo contains the code that:
1. Splits the big CSV files into small ones by countries for transparency and simple diff process
2. Generates JSON-LD files for publication


To update the JSON-LD files to a new version published on https://unece.org/trade/cefact/UNLOCODE-Download follow the steps:
1. Clone the repo and create a new branch named `release-XXXX-Y`, where XXXX - year, Y - version (1 or 2). For example `release-2023-2`:
   ```
   git clone https://github.com/uncefact/vocab-locode.git
   cd vocab-locode
   git checkout release-2023-2
   ```
2. Download the zip archive with CSV files from https://unece.org/trade/cefact/UNLOCODE-Download and extract it to a dedicated folder under`vocab-locode/scripts/src/main/resources`
   ```
   unzip loc232csv.zip -d vocab-locode/scripts/src/main/resources/loc232csv
   ```
3. Update the input directory in the package.yml workflow:
   ```
      directory:
        type: choice
        description: 'The location of input csv files'
        required: false
        options:
          - loc232csv
    ```
4.  Commit the csv files and updated package.yml file:
    ```
    git add .github/workflows/package.yml
    git add scripts/src/main/resources/loc232csv
    git commit -m " feat: add v2023-2 files"
    git push origin release-2023-2
    ```
5.  Go to https://github.com/uncefact/vocab-locode/actions, choose `JSON-LD vocabulary generation from CSV files` and click on `Run workflow`, choose the release-2023-2 branch and split mode:
    ![image](https://github.com/uncefact/vocab-locode/assets/6890602/c83c22bb-5dd5-4109-b9f1-13bb4e14b4e2)
    
    This will generate csv files by country and put them under `locodes` directory. Review the diff in the commit to see the changes.
7. Run the workflow again, choose release-2032-2 branch again but json-ld mode this time. It will generate the JSON-LD files and put them under vocab directory.
   The files unlocode-subdivisions.jsonld and unlocode.jsonld are too big to show the diff in GitHub UI, so checkout *-diff.txt files to see what was changed there.
8. Create a pull request to merge the changes into the main branch:   
   ![image](https://github.com/uncefact/vocab-locode/assets/6890602/baf0f5e2-46e3-4ec5-8780-0e2b50bf6288)
   
9. Squash commits and merge the branch into main
10.  Go to https://github.com/uncefact/vocab-locode/actions, choose `release` and on `Run workflow`, set tag to `vXXXX-Y` and tag message to `Update XXXX-Y release`:    
     ![image](https://github.com/uncefact/vocab-locode/assets/6890602/27acd588-ef41-43a8-8a66-abf5bb3325bc)
11. The release workflow will create a release and tag and trigger a workflow in https://github.com/uncefact/vocabulary-outputs/ that will raise a new PR to update the the json-ld vocabulary files and md files.
    Find it on https://github.com/uncefact/vocabulary-outputs/pull/ with the name `chore: update UN/LOCODEs vocabulary`.



