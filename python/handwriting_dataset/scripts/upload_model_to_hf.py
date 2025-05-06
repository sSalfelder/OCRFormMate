from huggingface_hub import HfApi

model_path = "../../models/fhswf_trocr"


api = HfApi()

api.create_repo(
    repo_id="sSalfelder/formfelder_trocr",
    repo_type="model",
    private=True
)
api.upload_folder(
    folder_path=model_path,
    repo_id="sSalfelder/formfelder_trocr",
    repo_type="model"
)
